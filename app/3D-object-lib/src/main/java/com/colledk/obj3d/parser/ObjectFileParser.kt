package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import timber.log.Timber
import java.io.InputStream

internal class ObjectFileParser {

    fun parseFile(fileId: Int, context: Context): ObjectData{
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return parseLines(lines = lines)
    }

    fun parseStream(inputStream: InputStream): ObjectData{
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return parseLines(lines = lines)
    }

    private fun parseLines(lines: List<String>): ObjectData{
        val vertices = mutableListOf<VertexData>()
        val faces = mutableListOf<FaceData>()

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
                                    index = vertices.size + 1,
                                    x = vertexData[0],
                                    y = vertexData[1],
                                    z = vertexData[2],
                                )
                            )
                        }
                        4 -> { // When we have 4 inputs from the line then we include the w value
                            vertices.add(
                                VertexData(
                                    index = vertices.size + 1,
                                    x = vertexData[0],
                                    y = vertexData[1],
                                    z = vertexData[2],
                                    w = vertexData[3],
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
                                vertexIndeces = lineData.map { it.value.split("/")[0].toShort() }
                            )
                        )
                    } else {
                        faces.add(
                            FaceData(
                                lineData.map { it.value.toShort() }
                            )
                        )
                    }
                }
                else -> {
                    Timber.d("Unknown data for line $line")
                }
            }
        }

        Timber.d("Retrieved data from file ${vertices.joinToString()}\n${faces.joinToString()}")
        return ObjectData(
            vertices = vertices,
            faces = faces
        )
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+([-+]?[0-9]*([ /])?)+".toRegex()
        val FACE_DATA_REGEX = "([-+]?[0-9]*([/]([-+]?[0-9])*)+|([-+]?[0-9])+)".toRegex()
    }
}