package com.colledk.obj3d.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colledk.obj3d.db.model.FaceLocal
import com.colledk.obj3d.db.model.ObjectLocal
import com.colledk.obj3d.db.model.ObjectNameLocal
import com.colledk.obj3d.db.model.VertexLocal
import com.colledk.obj3d.db.model.VertexNormalLocal

@Dao
interface ObjectDao {
    @Transaction
    @Query("SELECT * FROM ObjectNameLocal")
    suspend fun getAllObjects(): List<ObjectLocal>?

    @Transaction
    @Query("SELECT * FROM ObjectNameLocal WHERE name = :name")
    suspend fun getSpecificObject(name: String): ObjectLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectName(vararg objects: ObjectNameLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectFaces(vararg faces: FaceLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectVertexes(vararg vertexes: VertexLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectVertexNormals(vararg vertexNormals: VertexNormalLocal)

    @Transaction
    suspend fun insertObject(`object`: ObjectLocal){
        insertObjectName(`object`.`object`)
        insertObjectFaces(*`object`.faces.toTypedArray())
        insertObjectVertexes(*`object`.vertices.toTypedArray())
        insertObjectVertexNormals(*`object`.vertexNormals.toTypedArray())
    }
}