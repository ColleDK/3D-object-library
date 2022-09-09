package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import com.colledk.obj3d.parser.data.VertexNormalData
import timber.log.Timber
import java.io.InputStream

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
        lines.forEachIndexed { index, line ->
            when{
                line.matches(VERTEX_REGEX) -> {
                    val lineData = VERTEX_DATA_REGEX.findAll(line).toList()
                    Timber.d("Current line data for vertex ${lineData.map { it.value }}")
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
                    Timber.d("Current line data for face ${lineData.map { it.value }}")

                    // Check if we only have face vertex data
                    if (lineData.map { it.value }.all { it.contains("/") }){
                        Timber.d("Current line data for face includes /")
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
                line.matches(VERTEX_NORMAL_REGEX) -> {
                    val lineData = VERTEX_NORMAL_DATA_REGEX.findAll(line).toList()
                    Timber.d("Current line data for vertex normal ${lineData.map { it.value }}")
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
                else -> {
                    Timber.d("Unknown data for line $line")
                }
            }
        }

        Timber.d("Retrieved data from file ${vertices.joinToString()}\n${faces.joinToString()}\n${vertexNormals.joinToString()}")
        return ObjectData(
            vertices = vertices,
            faces = faces,
            vertexNormals = vertexNormals
        )
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+([-+]?[0-9]*([ /])?)+".toRegex()
        val FACE_DATA_REGEX = "([-+]?[0-9]*([/]([-+]?[0-9])*)+|([-+]?[0-9])+)".toRegex()
        val VERTEX_NORMAL_REGEX = "vn[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_NORMAL_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
    }
}