package com.colledk.obj3d.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colledk.obj3d.db.model.ObjectLocal

@Dao
interface ObjectDao {
    @Query("SELECT * FROM objectlocal")
    fun getAllObjects(): List<ObjectLocal>

    @Query("SELECT * FROM objectlocal WHERE name = :name")
    fun getSpecificObject(name: String): ObjectLocal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg objects: ObjectLocal)

    @Delete
    fun deleteObject(`object`: ObjectLocal)
}