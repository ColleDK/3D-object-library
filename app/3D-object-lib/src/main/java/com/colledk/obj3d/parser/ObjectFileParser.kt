package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.VertexData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

internal class ObjectFileParser {

    fun parseFile(fileId: Int, context: Context) {
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        parseLines(lines = lines)
    }

    fun parseStream(inputStream: InputStream){
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        parseLines(lines = lines)
    }

    private fun parseLines(lines: List<String>){
        val vertices = mutableListOf<VertexData>()
        val faces = mutableListOf<FaceData>()

        lines.forEachIndexed { index, line ->
            when{
                line.matches(VERTEX_REGEX) -> {
                    val lineData = VERTEX_DATA_REGEX.findAll(line).toList()
                    Timber.d("Current line data for vertex ${lineData.map { it.value }}")

                }
                line.matches(FACE_REGEX) -> {
                    val lineData = FACE_DATA_REGEX.findAll(line).toList()
                    Timber.d("Current line data for face ${lineData.map { it.value }}")
                }
                else -> {
                    Timber.d("Unknown data for line $line")
                }
            }
        }
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ]*)?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+([-+]?[0-9]*([ /])?)+".toRegex()
        val FACE_DATA_REGEX = "([-+]?[0-9]*([/]([-+]?[0-9])*)+|([-+]?[0-9])+)".toRegex()
    }
}