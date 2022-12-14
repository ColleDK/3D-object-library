package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.model.FaceData
import com.colledk.obj3d.parser.model.ObjectData
import com.colledk.obj3d.parser.model.VertexData
import com.colledk.obj3d.parser.model.VertexNormalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max

internal class ObjectFileParser {
    suspend fun parseURL(url: String, warnOnThreshold: Boolean): ObjectData = withContext(Dispatchers.IO) {
        // Get the data from the url
        val apiService = ApiClient.getClient()
        val body = apiService.getFromUrl(url = url).body()

        // Create an inputstream from the responsebody
        body?.byteStream()?.let {
            return@withContext parseStream(inputStream = it, warnOnThreshold = warnOnThreshold)
        } ?: run {
            Timber.e("Cannot load file from url")
            return@withContext parseLines(lines = listOf())
        }
    }

    suspend fun parseFile(fileId: Int, context: Context, warnOnThreshold: Boolean): ObjectData = withContext(Dispatchers.IO){
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Get the object data from the parsed lines
        return@withContext parseStream(inputStream = inputStream, warnOnThreshold = warnOnThreshold)
    }

    suspend fun parseStream(inputStream: InputStream, warnOnThreshold: Boolean): ObjectData = withContext(Dispatchers.IO){
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        if(lines.size > LINE_THRESHOLD){
            if (warnOnThreshold){
                Timber.w("WARNING: Reading a file with a line count of ${lines.size}. The required data for displaying this object might not be able to allocate, and could lead to crashes!")
            } else {
                return@withContext parseLines(listOf())
            }
        }

        // Get the object data from the parsed lines
        return@withContext parseLines(lines = lines)
    }

    private suspend fun parseLines(lines: List<String>): ObjectData = withContext(Dispatchers.IO){
        var currentMaterialName = ""
        var currentObjectGroupName = ""

        val faces = mutableListOf<FaceData>()
        val vertices = mutableListOf<VertexData>()
        val normals = mutableListOf<VertexNormalData>()

        lines.forEachIndexed { _, line ->
            when{
                line.matches(VERTEX_REGEX) -> {
                    val currentVertexData = getVertexData(
                        line = line,
                    )

                    vertices.add(currentVertexData)
                }
                line.matches(FACE_REGEX) -> {
                    val currentFaceData = getFaceData(
                        line = line,
                        materialName = currentMaterialName,
                        objectGroupName = currentObjectGroupName
                    )

                    faces.add(currentFaceData)
                }
                line.matches(FACE_REGEX_NOT_TRIANGULATED) -> { // When a face has n >= 4 vertices then we need to triangulate the polygon
                    val currentFaceData = getFaceDataTriangulated(
                        line = line,
                        vertices = vertices,
                        materialName = currentMaterialName,
                        objectGroupName = currentObjectGroupName
                    )

                    faces.addAll(currentFaceData)
                }
                line.matches(VERTEX_NORMAL_REGEX) -> {
                    val currentVertexNormalData = getVertexNormalData(
                        line = line
                    )

                    normals.add(currentVertexNormalData)
                }
                line.matches(OBJECT_REGEX) -> {
                    val currentObjectName = getObjectData(line)
                    currentObjectGroupName = currentObjectName
                }
                line.matches(MATERIAL_REGEX) -> {
                    val lineData = MATERIAL_DATA_REGEX.findAll(line).map { it.value }.filterNot { it == "usemtl" }.toList()
                    currentMaterialName = lineData.firstOrNull() ?: ""
                }
                else -> { /* Not a useful command so we move on */ }
            }
        }

        // Convert all the vertices into homogenous coordinate system
        val maxVertexX = vertices.maxOf { abs(it.x) }
        val maxVertexY = vertices.maxOf { abs(it.y) }
        val maxVertexZ = vertices.maxOf { abs(it.z) }
        val maxVertexVal = max(maxVertexX, max(maxVertexY, maxVertexZ))

        val homogenousVertices = vertices.map { it / maxVertexVal }

        return@withContext ObjectData(
            vertices = homogenousVertices,
            vertexNormals = normals,
            faces = faces
        )
    }

    private fun getVertexData(line: String): VertexData {
        val lineData = VERTEX_DATA_REGEX.findAll(line).map { it.value.toFloat() }.toList()
        return when(lineData.size){
            3 -> { // When we have 3 inputs we exclude the w value
                VertexData(
                    x = lineData[0],
                    y = lineData[1],
                    z = lineData[2],
                )
            }
            4 -> { // When we have 4 inputs from the line then we include the w value
                VertexData(
                    x = lineData[0],
                    y = lineData[1],
                    z = lineData[2],
                    w = lineData[3],
                )

            }
            else -> {
                VertexData(
                    0f,
                    0f,
                    0f,
                )
            }
        }
    }

    private fun getFaceData(line: String, materialName: String, objectGroupName: String): FaceData {
        val lineData = FACE_DATA_REGEX.findAll(line).map { it.value }.toList()

        // Check if we only have face vertex data
        return if (lineData.all { it.contains("/") }){
            FaceData(
                vertexIndeces = lineData.map { it.split("/")[0].toInt() - 1 },
                vertexNormalIndeces = lineData.map { it.split("/")[2].toInt() - 1 },
                materialName = materialName,
                objectGroupName = objectGroupName
            )

        } else {
            FaceData(
                vertexIndeces = lineData.map { it.toInt() - 1 },
                materialName = materialName,
                objectGroupName = objectGroupName
            )
        }
    }

    private fun getFaceDataTriangulated(line: String, vertices: List<VertexData>, materialName: String, objectGroupName: String): List<FaceData> {
        val faces = mutableListOf<FaceData>()

        // First we retrieve the data from the line
        val lineData = FACE_DATA_REGEX.findAll(line).map { it.value }.toList()

        // First we retrieve the indeces for the vertices
        val indeces = if (lineData.all { it.contains("/") }){
            lineData.map { it.split("/")[0].toInt() - 1 }
        } else {
            lineData.map { it.toInt() - 1 }
        }

        // First we retrieve the indeces for the normals
        val normalIndeces: MutableList<Int>? = if (lineData.all { it.contains("/") }){
            lineData.map { it.split("/")[2].toInt() - 1 }.toMutableList()
        } else {
            null
        }

        // Then we find the vertices that are used in the face
        val positions = vertices.filterIndexed{ ind, _ -> indeces.contains(ind) }

        val indexList = mutableListOf<Int>()
        indexList.addAll(indeces)

        // We iterate the vertices until we have a triangle
        var currentIndex = 0
        while(indexList.size > 3){
            // Get the positions for the triangle
            val prev = positions[indexList.indexOf(indexList[(currentIndex - 1 + indexList.size) % indexList.size])]
            val curr = positions[indexList.indexOf(indexList[currentIndex])]
            val next = positions[indexList.indexOf(indexList[(currentIndex + 1) % indexList.size])]

            // Check if the vertex is convex (interior angle is less than ?? radian)
            // Define two vectors AB AC
            val vector1 = curr - prev
            val vector2 = curr - next

            // Calculate the inner angle of the vertex A between B and C
            val angle = vector1.angleBetween(vector2)

            // If the angle is below 180 then it is a convex vertex which means that we might be able to triangulate it
            if (angle < 180){

                // We need to iterate all other points in the face and determine if any points is inside the triangle ABC
                var notInTriangle = true
                for (i in positions.indices){
                    when (i) {
                        currentIndex -> { /* Don't include the triangle points */ }
                        (currentIndex + 1) % positions.size -> { /* Don't include the triangle points */ }
                        (currentIndex - 1 + positions.size) % positions.size -> { /* Don't include the triangle points */ }
                        else -> {
                            // Calculate the barycentric coordinates to determine if point p is inside ABC
                            val p = positions[i]
                            val alpha = ((curr.y - next.y) * (p.x - next.x) + (next.x - curr.x) * (p.y - next.y)) /
                                    ((curr.y - next.y) * (prev.x - next.x) + (next.x - curr.x) * (prev.y - next.y))

                            val beta = ((next.y - prev.y) * (p.x - next.x) + (prev.x - next.x) * (p.y - next.y)) /
                                    ((curr.y - next.y) * (prev.x - next.x) + (next.x - curr.x) * (prev.y - next.y))

                            val gamma = 1 - alpha - beta

                            // If alpha, beta and gamma is larger than 0 then the point is within the triangle
                            if (alpha > 0 && beta > 0 && gamma > 0){
                                // Stop iteration and move to next vertex
                                notInTriangle = false
                                break
                            }
                        }
                    }
                }

                // If no points are within the triangle that means we can triangulate the 3 vertices
                if (notInTriangle){
                    // We add the face draw order to the triangle list
                    normalIndeces?.let {
                        faces.add(
                            FaceData(
                                vertexIndeces = listOf(
                                    indexList[(currentIndex - 1 + indexList.size) % indexList.size],
                                    indexList[currentIndex],
                                    indexList[(currentIndex + 1) % indexList.size],
                                ),
                                vertexNormalIndeces = listOf(
                                    it[(currentIndex - 1 + indexList.size) % indexList.size],
                                    it[currentIndex],
                                    it[(currentIndex + 1) % indexList.size],
                                ),
                                materialName = materialName,
                                objectGroupName = objectGroupName
                            )
                        )
                    } ?: run {
                        faces.add(
                            FaceData(
                                vertexIndeces = listOf(
                                    indexList[(currentIndex - 1 + indexList.size) % indexList.size],
                                    indexList[currentIndex],
                                    indexList[(currentIndex + 1) % indexList.size],
                                ),
                                materialName = materialName,
                                objectGroupName = objectGroupName
                            )
                        )
                    }
                    // And remove the vertex that was used to triangulate
                    indexList.removeAt(currentIndex)
                    normalIndeces?.removeAt(currentIndex)
                    // Reset the list to find next triangulation
                    currentIndex = 0
                } else {
                    // If points are within the triangle we move to next vertex
                    currentIndex = (currentIndex+1)%indexList.size
                }
            } else {
                // If angle is larger than 180 that means we cannot triangulate the vertex and we move to the next
                currentIndex = (currentIndex+1)%indexList.size
            }
        }

        // Once we only have 3 vertices left (single triangle) and completely triangulated the face
        // We add the last one
        faces.add(
            FaceData(
                vertexIndeces = indexList,
                vertexNormalIndeces = normalIndeces,
                materialName = materialName
            )
        )

        return faces
    }

    private fun getVertexNormalData(line: String): VertexNormalData {
        val lineData = VERTEX_NORMAL_DATA_REGEX.findAll(line).map { it.value.toFloat() }.toList()

        return when(lineData.size){
            3 -> {
                VertexNormalData(
                    x = lineData[0],
                    y = lineData[1],
                    z = lineData[2],
                    null,
                )

            }
            4 -> {
                VertexNormalData(
                    x = lineData[0],
                    y = lineData[1],
                    z = lineData[2],
                    w = lineData[3],
                )

            }
            else -> {
                VertexNormalData(
                    0f,
                    0f,
                    0f,
                )
            }
        }
    }

    private fun getObjectData(line: String): String {
        val lineData = OBJECT_REGEX.find(line)
        lineData?.let {
            val (objectName) = it.destructured
            return objectName
        }
        return ""
    }

    companion object{
        val VERTEX_REGEX = "v\\s+([-+]?\\d+(.\\d+)?(\\s*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?\\d+(.\\d+)?".toRegex()
        val FACE_REGEX = "f\\s+(\\d+([/]+\\d+)*\\s*){3}".toRegex()
        val FACE_REGEX_NOT_TRIANGULATED = "f\\s+(\\d+([/]+\\d+)*\\s*)+".toRegex()
        val FACE_DATA_REGEX = "(\\d+([/]+\\d+)*)".toRegex()
        val VERTEX_NORMAL_REGEX = "vn\\s+([-+]?\\d+(.\\d+)?(\\s*)?){3,4}".toRegex()
        val VERTEX_NORMAL_DATA_REGEX = "[-+]?\\d+(.\\d+)?".toRegex()
        val OBJECT_REGEX = "g\\s+([\\w_\\s]+)".toRegex()
        val OBJECT_DATA_REGEX = "[\\w_\\s]+".toRegex()
        val MATERIAL_REGEX = "usemtl\\s+[\\w:\\s]+".toRegex()
        val MATERIAL_DATA_REGEX = "[\\w:]+".toRegex()
        const val LINE_THRESHOLD = 125000
    }
}