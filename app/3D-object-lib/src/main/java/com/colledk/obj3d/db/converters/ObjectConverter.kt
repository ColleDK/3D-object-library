package com.colledk.obj3d.db.converters

import androidx.room.TypeConverter
import com.colledk.obj3d.db.model.FaceLocal
import com.colledk.obj3d.db.model.VertexLocal
import com.colledk.obj3d.db.model.VertexNormalLocal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ObjectConverter {
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        val type = object : TypeToken<List<Int>>(){}.type
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun toIntList(json: String): List<Int> {
        val type = object : TypeToken<List<Int>>(){}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromFloatArray(list: FloatArray): String {
        val type = object : TypeToken<FloatArray>(){}.type
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun toFloatArray(json: String): FloatArray {
        val type = object : TypeToken<FloatArray>(){}.type
        return Gson().fromJson(json, type)
    }
}