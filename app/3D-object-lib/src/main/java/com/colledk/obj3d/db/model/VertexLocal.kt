package com.colledk.obj3d.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VertexLocal(
    @PrimaryKey(autoGenerate = true) var index: Int = 0,
    val objectName: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float?,
)
