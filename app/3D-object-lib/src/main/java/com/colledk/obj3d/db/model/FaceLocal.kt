package com.colledk.obj3d.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FaceLocal(
    @PrimaryKey(autoGenerate = true) var index: Int = 0,
    val objectName: String,
    val vertexIndeces: List<Int>,
    val vertexNormalIndeces: List<Int>? = null,
    val color: FloatArray = floatArrayOf(0.7f, 0.7f, 0.7f),
    val materialName: String = "",
    val objectGroupName: String = "",
)
