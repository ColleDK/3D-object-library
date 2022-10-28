package com.colledk.obj3d.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ObjectNameLocal(
    @PrimaryKey val name: String
)

data class ObjectLocal(
    @Embedded val `object`: ObjectNameLocal,
    @Relation(
        parentColumn = "name",
        entityColumn = "objectName"
    )
    val faces: List<FaceLocal>,
    @Relation(
        parentColumn = "name",
        entityColumn = "objectName"
    )
    val vertices: List<VertexLocal>,
    @Relation(
        parentColumn = "name",
        entityColumn = "objectName"
    )
    val vertexNormals: List<VertexNormalLocal>,
)