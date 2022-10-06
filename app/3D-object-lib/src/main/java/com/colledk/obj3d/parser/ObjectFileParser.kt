package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import com.colledk.obj3d.parser.data.VertexNormalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

internal class ObjectFileParser {

    suspend fun parseFile(fileId: Int, context: Context, scale: Int = 1, onFinish: () -> Unit): ObjectData = withContext(Dispatchers.IO){
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return@withContext parseLines(lines = lines, onFinish = onFinish)
    }

    suspend fun parseStream(inputStream: InputStream, scale: Int = 1, onFinish: () -> Unit): ObjectData = withContext(Dispatchers.IO){
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return@withContext parseLines(lines = lines, scale = scale, onFinish = onFinish)
    }

    private suspend fun parseLines(lines: List<String>, scale: Int = 1, onFinish: () -> Unit): ObjectData = withContext(Dispatchers.IO){
        val vertices = mutableListOf<VertexData>()
        val faces = mutableListOf<FaceData>()
        val vertexNormals = mutableListOf<VertexNormalData>()

        var currentColor = floatArrayOf(0.7f, 0.7f, 0.7f)
        var currentDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
        var currentMaterialName = ""

        lines.forEachIndexed { _, line ->
            when{
                line.matches(VERTEX_REGEX) -> {
                    val lineData = VERTEX_DATA_REGEX.findAll(line).toList()
                    val vertexData = lineData.map { it.value.toFloat() }
                    when(vertexData.size){
                        3 -> { // When we have 3 inputs we exclude the w value
                            vertices.add(
                                VertexData(
                                    x = vertexData[0] / scale,
                                    y = vertexData[1] / scale,
                                    z = vertexData[2] / scale,
                                )
                            )
                        }
                        4 -> { // When we have 4 inputs from the line then we include the w value
                            vertices.add(
                                VertexData(
                                    x = vertexData[0] / scale,
                                    y = vertexData[1] / scale,
                                    z = vertexData[2] / scale,
                                    w = vertexData[3] / scale,
                                )
                            )
                        }
                    }
                }
                line.matches(FACE_REGEX) -> {
                    val lineData = FACE_DATA_REGEX.findAll(line).toList()

                    // Check if we only have face vertex data
                    if (lineData.map { it.value }.all { it.contains("/") }){
                        faces.add(
                            FaceData(
                                vertexIndeces = lineData.map { it.value.split("/")[0].toInt() - 1 },
                                vertexNormalIndeces = lineData.map { it.value.split("/")[2].toInt() - 1 },
                                color = currentColor,
                                materialName = currentMaterialName
                            )
                        )
                    } else {
                        faces.add(
                            FaceData(
                                vertexIndeces = lineData.map { it.value.toInt() - 1 },
                                color = currentColor,
                                materialName = currentMaterialName
                            )
                        )
                    }
                }
                line.matches(FACE_REGEX_NOT_TRIANGULATED) -> { // When a face has n >= 4 vertices then we need to triangulate the polygon
                    // First we retrieve the data from the line
                    val lineData = FACE_DATA_REGEX.findAll(line).toList()

                    val indeces = if (lineData.map { it.value }.all { it.contains("/") }){
                        // First we retrieve the indeces for the vertices
                        lineData.map { it.value.split("/")[0].toInt() - 1 }
                    } else {
                        // First we retrieve the indeces for the vertices
                        lineData.map { it.value.toInt() - 1 }
                    }

                    val normalIndeces: MutableList<Int>? = if (lineData.map { it.value }.all { it.contains("/") }){
                        // First we retrieve the indeces for the vertices
                        lineData.map { it.value.split("/")[2].toInt() - 1 }.toMutableList()
                    } else {
                        // First we retrieve the indeces for the vertices
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

                        // Check if the vertex is convex (interior angle is less than π radian)
                        // Define two vectors AB AC
                        val vector1 = curr - prev

                        val vector2 = curr - next

                        // Calculate the inner angle of the vertex A between B and C
                        val dot = vector1.x * vector2.x + vector1.y * vector2.y + vector1.y * vector2.y
                        val length1 = sqrt(vector1.x.toDouble().pow(2) + vector1.y.toDouble().pow(2) + vector1.z.toDouble().pow(2))
                        val length2 = sqrt(vector2.x.toDouble().pow(2) + vector2.y.toDouble().pow(2) + vector2.z.toDouble().pow(2))
                        val theta = dot.toDouble() / (length1 * length2)

                        val angle = Math.toDegrees(
                            acos(
                                theta.coerceIn(-1.0, 1.0)
                            )
                        )

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
                                            color = currentColor,
                                            materialName = currentMaterialName
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
                                            color = currentColor,
                                            materialName = currentMaterialName
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
                            color = currentColor,
                            materialName = currentMaterialName
                        )
                    )
                }
                line.matches(VERTEX_NORMAL_REGEX) -> {
                    val lineData = VERTEX_NORMAL_DATA_REGEX.findAll(line).toList()
                    val vertexNormalData = lineData.map { it.value.toFloat() }

                    when(vertexNormalData.size){
                        3 -> {
                            vertexNormals.add(
                                VertexNormalData(
                                    x = vertexNormalData[0],
                                    y = vertexNormalData[1],
                                    z = vertexNormalData[2],
                                    null,
                                )
                            )
                        }
                        4 -> {
                            vertexNormals.add(
                                VertexNormalData(
                                    x = vertexNormalData[0],
                                    y = vertexNormalData[1],
                                    z = vertexNormalData[2],
                                    w = vertexNormalData[3],
                                )
                            )
                        }
                    }
                }
                line.matches(OBJECT_REGEX) -> {
                    val rand = Random.Default
                    var r = rand.nextFloat()
                    var g = rand.nextFloat()
                    var b = rand.nextFloat()
                    currentColor = floatArrayOf(r, g, b)

                    r = rand.nextFloat()
                    g = rand.nextFloat()
                    b = rand.nextFloat()
                    currentDiffuse = floatArrayOf(r, g, b)
                }
                line.matches(MATERIAL_REGEX) -> {
                    val lineData = MATERIAL_DATA_REGEX.findAll(line).map { it.value }.filterNot { it == "usemtl" }.toList()
                    Timber.d("New material $lineData")
                    currentMaterialName = lineData.firstOrNull() ?: ""
                }
                else -> { /* Not a useful command so we move on */ }
            }
        }
        return@withContext ObjectData(
            vertices = vertices,
            faces = faces,
            vertexNormals = vertexNormals
        ).also { onFinish() }
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+(\\d+([/]+\\d+)*[ ]*){3}".toRegex()
        val FACE_REGEX_NOT_TRIANGULATED = "f[ ]+(\\d+([/]+\\d+)*[ ]*)+".toRegex()
        val FACE_DATA_REGEX = "(\\d+([/]+\\d+)*)".toRegex()
        val VERTEX_NORMAL_REGEX = "vn[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_NORMAL_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val OBJECT_REGEX = "g[ ]+\\w+".toRegex()
        val MATERIAL_REGEX = "usemtl[ ]+\\w+".toRegex()
        val MATERIAL_DATA_REGEX = "\\w+".toRegex()
    }
}