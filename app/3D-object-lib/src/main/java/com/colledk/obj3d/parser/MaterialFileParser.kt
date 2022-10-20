package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.data.Material
import com.colledk.obj3d.parser.data.ObjectData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

internal class MaterialFileParser {

    suspend fun parseURL(url: String,onFinish: () -> Unit): List<Material> = withContext(Dispatchers.IO) {
        val apiService = ApiClient.getClient()
        val body = apiService.getFromUrl(url = url).body()

        body?.byteStream()?.let {
            return@withContext parseStream(inputStream = it, onFinish = onFinish)
        } ?: run {
            Timber.e("Cannot load file from url")
            return@withContext parseLines(lines = listOf(), onFinish = onFinish)
        }
    }

    suspend fun parseFile(fileId: Int, context: Context, onFinish: () -> Unit): List<Material> = withContext(Dispatchers.IO){
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return@withContext parseLines(lines = lines, onFinish = onFinish)
    }

    suspend fun parseStream(inputStream: InputStream, onFinish: () -> Unit): List<Material> = withContext(Dispatchers.IO){
        // Retrieve the lines of the file
        val lines = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { lines.add(it) }

        // Get the object data from the parsed lines
        return@withContext parseLines(lines = lines, onFinish = onFinish)
    }

    private suspend fun parseLines(lines: List<String>, onFinish: () -> Unit): List<Material> = withContext(Dispatchers.IO){
        val materials = mutableListOf<Material>()
        lines.forEach { line ->
            when{
                line.matches(MATERIAL_NAME_REGEX) -> {
                    val currentName = getNewMaterialName(line)
                    materials.add(Material(name = currentName))
                }
                line.matches(SHININESS_REGEX) -> {
                    val currentShininess = getMaterialShininess(line)
                    materials.lastOrNull()?.shininess = currentShininess
                }
                line.matches(AMBIENT_REGEX) -> {
                    val currentAmbient = getMaterialAmbient(line)
                    materials.lastOrNull()?.ambient = currentAmbient
                }
                line.matches(DIFFUSE_REGEX) -> {
                    val currentDiffuse = getMaterialDiffuse(line)
                    materials.lastOrNull()?.diffuse = currentDiffuse
                }
                line.matches(SPECULAR_REGEX) -> {
                    val currentSpecular = getMaterialSpecular(line)
                    materials.lastOrNull()?.specular = currentSpecular
                }
                line.matches(EMISSIVE_REGEX) -> {
                    val currentEmissive = getMaterialEmissive(line)
                    materials.lastOrNull()?.emissive = currentEmissive
                }
                line.matches(OPTICAL_DENSITY_REGEX) -> {
                    val currentOpticalDensity = getMaterialOpticalDensity(line)
                    materials.lastOrNull()?.opticalDensity = currentOpticalDensity
                }
                line.matches(DISSOLVE_REGEX) -> {
                    val currentOpacity = getMaterialOpacity(line)
                    materials.lastOrNull()?.opacity = currentOpacity
                }
                line.matches(ILLUM_REGEX) -> {
                    val currentIllum = getMaterialIllum(line)
                    materials.lastOrNull()?.illum = currentIllum
                }
            }
        }

        return@withContext materials.also { onFinish() }
    }

    private fun getNewMaterialName(line: String): String{
        val lineData = STRING_DATA_REGEX.findAll(line).map { it.value }
        return lineData.filterNot { it == "newmtl" }.firstOrNull() ?: ""
    }

    private fun getMaterialShininess(line: String): Float {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.firstOrNull() ?: 1f
    }

    private fun getMaterialAmbient(line: String): FloatArray {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.toList().toFloatArray()
    }

    private fun getMaterialDiffuse(line: String): FloatArray {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.toList().toFloatArray()
    }

    private fun getMaterialSpecular(line: String): FloatArray {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.toList().toFloatArray()
    }

    private fun getMaterialEmissive(line: String): FloatArray {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.toList().toFloatArray()
    }

    private fun getMaterialOpticalDensity(line: String): Float {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.firstOrNull() ?: 1f
    }

    private fun getMaterialOpacity(line: String): Float {
        val lineData = FLOAT_DATA_REGEX.findAll(line).map { it.value.toFloat() }
        return lineData.firstOrNull() ?: 1f
    }

    private fun getMaterialIllum(line: String): Int {
        val lineData = INT_DATA_REGEX.findAll(line).map { it.value.toInt() }
        return lineData.firstOrNull() ?: 1
    }

    companion object {
        val MATERIAL_NAME_REGEX = "newmtl[ ]+[\\w: ]+".toRegex()
        val SHININESS_REGEX = "Ns[ ]+\\d+.\\d+".toRegex()
        val AMBIENT_REGEX = "Ka([ ]+\\d+.\\d+){3}".toRegex()
        val DIFFUSE_REGEX = "Kd([ ]+\\d+.\\d+){3}".toRegex()
        val SPECULAR_REGEX = "Ks([ ]+\\d+.\\d+){3}".toRegex()
        val EMISSIVE_REGEX = "Ke([ ]+\\d+.\\d+){3}".toRegex()
        val OPTICAL_DENSITY_REGEX = "Ni[ ]+\\d+.\\d+".toRegex()
        val DISSOLVE_REGEX = "d[ ]+\\d+.\\d+".toRegex()
        val ILLUM_REGEX = "illum[ ]+\\d+".toRegex()
        val FLOAT_DATA_REGEX = "\\d+.\\d+".toRegex()
        val STRING_DATA_REGEX = "[\\w:]+".toRegex()
        val INT_DATA_REGEX = "\\d+".toRegex()
    }
}