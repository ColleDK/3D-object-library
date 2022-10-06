package com.colledk.obj3d.parser.data

data class Material(
    val name: String = "",
    var shininess: Float = 1f,
    var ambient: FloatArray = floatArrayOf(1f, 1f, 1f),
    var diffuse: FloatArray = floatArrayOf(1f, 1f, 1f),
    var specular: FloatArray = floatArrayOf(1f, 1f, 1f),
    var emissive: FloatArray = floatArrayOf(1f, 1f, 1f),
    var opticalDensity: Float = 1f,
    var opacity: Float = 1f,
    var illum: Int = 1
)
