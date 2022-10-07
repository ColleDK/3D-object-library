package com.colledk.obj3d.parser.data

data class Material(
    val name: String = "",
    var shininess: Float = 400f,
    var ambient: FloatArray = floatArrayOf(0f, 0f, 0f),
    var diffuse: FloatArray = floatArrayOf(1f, 1f, 1f),
    var specular: FloatArray = floatArrayOf(1f, 1f, 1f),
    var emissive: FloatArray = floatArrayOf(0f, 0f, 0f),
    var opticalDensity: Float = 1f,
    var opacity: Float = 1f,
    var illum: Int = 1
)
