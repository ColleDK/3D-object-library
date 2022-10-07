package com.colledk.obj3d.parser.data

internal data class ObjectData(
    val vertices: List<VertexData> = listOf(),
    val vertexNormals: List<VertexNormalData> = listOf(),
    val faces: List<FaceData> = listOf(),
)
