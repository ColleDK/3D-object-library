package com.colledk.obj3d.parser.data

internal data class FaceData(
    val vertexIndeces: List<Int>,
    val vertexNormalIndeces: List<Int>? = null,
    val color: FloatArray = floatArrayOf(0.7f, 0.7f, 0.7f),
    val materialName: String = ""
)
