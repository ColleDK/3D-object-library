package com.colledk.obj3d.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ObjectLocal(
    @PrimaryKey val name: String,
    val vertices: List<VertexLocal> = listOf(),
    val vertexNormals: List<VertexNormalLocal> = listOf(),
    val faces: List<FaceLocal> = listOf(),
)