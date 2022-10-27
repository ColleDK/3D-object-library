package com.colledk.obj3d.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colledk.obj3d.db.converters.ObjectConverter
import com.colledk.obj3d.db.model.FaceLocal
import com.colledk.obj3d.db.model.ObjectLocal
import com.colledk.obj3d.db.model.VertexLocal
import com.colledk.obj3d.db.model.VertexNormalLocal

@Database(entities = [ObjectLocal::class], version = 1)
@TypeConverters(ObjectConverter::class)
abstract class ObjectDatabase: RoomDatabase() {
    abstract fun objectDao(): ObjectDao
}