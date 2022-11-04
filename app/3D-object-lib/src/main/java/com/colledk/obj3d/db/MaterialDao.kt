package com.colledk.obj3d.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colledk.obj3d.db.model.MaterialLocal
import com.colledk.obj3d.db.model.MaterialsLocal
import com.colledk.obj3d.db.model.MaterialsNameLocal
import com.colledk.obj3d.db.model.ObjectNameLocal
import com.colledk.obj3d.parser.data.Material

@Dao
interface MaterialDao {
    @Transaction
    @Query("SELECT * FROM MaterialLocal")
    suspend fun getAllMaterials(): List<MaterialsLocal>?

    @Transaction
    @Query("SELECT * FROM MaterialsNameLocal WHERE name = :name")
    suspend fun getSpecificMaterial(name: String): MaterialsLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialName(vararg materials: MaterialsNameLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(vararg materials: MaterialLocal)

    @Transaction
    suspend fun insertMaterials(materials: List<MaterialLocal>) {
        insertMaterialName(materials = materials.map { MaterialsNameLocal(it.fileName) }.toTypedArray())
        insertMaterial(materials = materials.toTypedArray())
    }
}