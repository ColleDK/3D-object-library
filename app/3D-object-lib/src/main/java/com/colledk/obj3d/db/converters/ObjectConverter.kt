package com.colledk.obj3d.db.converters

import androidx.room.TypeConverter
import com.colledk.obj3d.db.model.FaceLocal
import com.colledk.obj3d.db.model.VertexLocal
import com.colledk.obj3d.db.model.VertexNormalLocal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ObjectConverter {
    @TypeConverter
    fun fromVertexLocalList(list: List<VertexLocal>): String {
        val type = object : TypeToken<List<VertexLocal>>(){}.type
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun toVertexLocalList(json: String): List<VertexLocal> {
        val type = object : TypeToken<List<VertexLocal>>(){}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromVertexNormalLocalList(list: List<VertexNormalLocal>): String {
        val type = object : TypeToken<List<VertexNormalLocal>>(){}.type
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun toVertexNormalLocalList(json: String): List<VertexNormalLocal> {
        val type = object : TypeToken<List<VertexNormalLocal>>(){}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromFaceLocal(list: List<FaceLocal>): String {
        val type = object : TypeToken<List<FaceLocal>>(){}.type
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun toFaceLocal(json: String): List<FaceLocal> {
        val type = object : TypeToken<List<FaceLocal>>(){}.type
        return Gson().fromJson(json, type)
    }
}