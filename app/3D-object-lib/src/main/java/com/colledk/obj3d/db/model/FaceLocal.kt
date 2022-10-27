package com.colledk.obj3d.db.model

data class FaceLocal(
    val vertexIndeces: List<Int>,
    val vertexNormalIndeces: List<Int>? = null,
    val color: FloatArray = floatArrayOf(0.7f, 0.7f, 0.7f),
    val materialName: String = ""
)
