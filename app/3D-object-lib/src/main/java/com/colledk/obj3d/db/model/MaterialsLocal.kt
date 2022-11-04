package com.colledk.obj3d.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class MaterialsNameLocal(
    @PrimaryKey val name: String
)

data class MaterialsLocal(
    @Embedded val materialName: MaterialsNameLocal,
    @Relation(
        parentColumn = "name",
        entityColumn = "fileName"
    )
    val materials: List<MaterialLocal>
)

