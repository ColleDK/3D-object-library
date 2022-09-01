package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.VertexData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ObjectFileParser {

    suspend fun parseFile(fileId: Int, context: Context) = withContext(Dispatchers.IO){
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        parseLines(lines = lines)
    }

    private suspend fun parseLines(lines: List<String>) = withContext(Dispatchers.IO){
        val vertices = mutableListOf<VertexData>()
        val faces = mutableListOf<FaceData>()

        lines.forEachIndexed { index, line ->
            when{
                line.matches(VERTEX_REGEX) -> {
                    val lineData = VERTEX_DATA_REGEX.findAll(line).toList()
                    Timber.d("Current line data for vertex $lineData")

                }
                line.matches(FACE_REGEX) -> {
                    val lineData = FACE_DATA_REGEX.findAll(line)
                    Timber.d("Current line data for face $lineData")
                }
                else -> {
                    Timber.d("Unknown data for line $line")
                }
            }
        }
    }

    companion object{
        val VERTEX_REGEX = "v[ ]+([-+]?[0-9]+(.[0-9]+)?([ ])?){3,4}".toRegex()
        val VERTEX_DATA_REGEX = "[-+]?[0-9]+(.[0-9]+)?".toRegex()
        val FACE_REGEX = "f[ ]+([-+]?[0-9]+([ /])?)+".toRegex()
        val FACE_DATA_REGEX = "[-+]?[0-9]+".toRegex()
    }
}