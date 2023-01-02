package com.colledk.obj3d.parser

import android.content.Context
import com.colledk.obj3d.parser.model.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

internal class MaterialFileParser {

    suspend fun parseURL(url: String, warnOnThreshold: Boolean): List<Material> = withContext(Dispatchers.IO) {
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

    suspend fun parseFile(fileId: Int, context: Context, warnOnThreshold: Boolean): List<Material> = withContext(Dispatchers.IO){
        // Create an input stream from the raw resource
        val inputStream = context.resources.openRawResource(fileId)

        // Get the object data from the parsed lines
        return@withContext parseStream(inputStream = inputStream, warnOnThreshold = warnOnThreshold)
    }

    suspend fun parseStream(inputStream: InputStream, warnOnThreshold: Boolean): List<Material> = withContext(Dispatchers.IO){
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

    private suspend fun parseLines(lines: List<String>): List<Material> = withContext(Dispatchers.IO){
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

        return@withContext materials
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
        val FLOAT_DATA_REGEX = "\\d+.\\d+".toRegex()
        val STRING_DATA_REGEX = "[\\w:]+".toRegex()
        val INT_DATA_REGEX = "\\d+".toRegex()
        val MATERIAL_NAME_REGEX = "newmtl\\s+[\\w:\\s]+".toRegex()
        val SHININESS_REGEX = "Ns\\s+$FLOAT_DATA_REGEX".toRegex()
        val AMBIENT_REGEX = "Ka(\\s+$FLOAT_DATA_REGEX){3}".toRegex()
        val DIFFUSE_REGEX = "Kd(\\s+$FLOAT_DATA_REGEX){3}".toRegex()
        val SPECULAR_REGEX = "Ks(\\s+$FLOAT_DATA_REGEX){3}".toRegex()
        val EMISSIVE_REGEX = "Ke(\\s+$FLOAT_DATA_REGEX){3}".toRegex()
        val OPTICAL_DENSITY_REGEX = "Ni\\s+$FLOAT_DATA_REGEX".toRegex()
        val DISSOLVE_REGEX = "d\\s+$FLOAT_DATA_REGEX".toRegex()
        val ILLUM_REGEX = "illum\\s+$INT_DATA_REGEX".toRegex()
        const val LINE_THRESHOLD = 175000
    }
}