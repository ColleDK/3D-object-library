package com.colledk.obj3d.parser.data

internal data class ObjectData(
    val vertices: List<VertexData>,
    val vertexNormals: List<VertexNormalData>,
    val faces: List<FaceData>
)
