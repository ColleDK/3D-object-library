package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import com.colledk.obj3d.parser.data.VertexNormalData
import timber.log.Timber
import java.io.InputStream
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

internal class ObjectFileParser {

    fun parseFile(fileId: Int, context: Context, scale: Int = 1): ObjectData{
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return parseLines(lines = lines)
    }

    fun parseStream(inputStream: InputStream, scale: Int = 1): ObjectData{
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return parseLines(lines = lines, scale = scale)
    }

    private fun parseLines(lines: List<String>, scale: Int = 1): ObjectData{
        val vertices = mutableListOf<VertexData>()
        val faces = mutableListOf<FaceData>()
        val vertexNormals = mutableListOf<VertexNormalData>()

        //
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
                                vertexNormalIndeces = lineData.map { it.value.split("/")[2].toInt() - 1 }
                            )
                        )
                    } else {
                        faces.add(
                            FaceData(
                                vertexIndeces = lineData.map { it.value.toInt() - 1 }
                            )
                        )
                    }
                }
                line.matches(FACE_REGEX_NOT_TRIANGULATED) -> { // When a face has n >= 4 vertices then we need to triangulate the polygon
                    // First we retrieve the data from the line
                    val lineData = FACE_DATA_REGEX.findAll(line).toList()
                    val triangles = mutableListOf<Int>()

                    val indeces = if (lineData.map { it.value }.all { it.contains("/") }){
                        // First we retrieve the indeces for the vertices
                        lineData.map { it.value.split("/")[0].toInt() - 1 }


                    } else {
                        // First we retrieve the indeces for the vertices
                        lineData.map { it.value.toInt() - 1 }
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
                        val vector1 = floatArrayOf(
                            curr.x - prev.x, //x
                            curr.y - prev.y, // y
                            curr.z - prev.z // z
                        )

                        val vector2 = floatArrayOf(
                            curr.x - next.x, //x
                            curr.y - next.y, // y
                            curr.z - next.z // z
                        )

                        // Calculate the inner angle of the vertex A between B and C
                        val dot = vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2]
                        val length1 = sqrt(vector1[0].toDouble().pow(2) + vector1[1].toDouble().pow(2) + vector1[2].toDouble().pow(2))
                        val length2 = sqrt(vector2[0].toDouble().pow(2) + vector2[1].toDouble().pow(2) + vector2[2].toDouble().pow(2))
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
                                triangles.add(
                                    indexList[(currentIndex - 1 + indexList.size) % indexList.size],
                                )
                                triangles.add(
                                    indexList[currentIndex],
                                )
                                triangles.add(
                                    indexList[(currentIndex + 1) % indexList.size],
                                )

                                // And remove the vertex that was used to triangulate
                                indexList.removeAt(currentIndex)
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
                    // we add the data to the face list
                    triangles.chunked(3).forEach { triangle ->
                        faces.add(
                            FaceData(
                                vertexIndeces = triangle
                            )
                        )
                    }
                    faces.add(
                        FaceData(
                            indexList
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
                else -> { /* Not a useful command so we move on */ }
            }
        }
        return ObjectData(
            vertices = vertices,
            faces = faces,
            vertexNormals = vertexNormals
        )
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+(\\d+([/]+\\d+)*[ ]*){3}".toRegex()
        val FACE_REGEX_NOT_TRIANGULATED = "f[ ]+(\\d+([/]+\\d+)*[ ]*)+".toRegex()
        val FACE_DATA_REGEX = "(\\d+([/]+\\d+)*)".toRegex()
        val VERTEX_NORMAL_REGEX = "vn[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_NORMAL_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
    }
}