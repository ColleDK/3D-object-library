package com.colledk.obj3d.db

import com.colledk.obj3d.db.model.FaceLocal
import com.colledk.obj3d.db.model.ObjectLocal
import com.colledk.obj3d.db.model.VertexLocal
import com.colledk.obj3d.db.model.VertexNormalLocal
import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import com.colledk.obj3d.parser.data.VertexNormalData

internal fun ObjectData.mapToLocal(name: String): ObjectLocal {
    return ObjectLocal(
        name = name,
        vertices = vertices.map { it.mapToLocal() },
        vertexNormals = vertexNormals.map { it.mapToLocal() },
        faces = faces.map { it.mapToLocal() }
    )
}

internal fun VertexData.mapToLocal(): VertexLocal {
    return VertexLocal(
        x = x,
        y = y,
        z = z,
        w = w,
    )
}

internal fun VertexNormalData.mapToLocal(): VertexNormalLocal {
    return VertexNormalLocal(
        x = x,
        y = y,
        z = z,
        w = w,
    )
}

internal fun FaceData.mapToLocal(): FaceLocal {
    return FaceLocal(
        vertexIndeces = vertexIndeces,
        vertexNormalIndeces = vertexNormalIndeces,
        color = color,
        materialName = materialName,
    )
}

internal fun ObjectLocal.mapToDomain(): ObjectData {
    return ObjectData(
        vertices = vertices.map { it.mapToDomain() },
        vertexNormals = vertexNormals.map { it.mapToDomain() },
        faces = faces.map { it.mapToDomain()}
    )
}

internal fun VertexLocal.mapToDomain(): VertexData {
    return VertexData(
        x = x,
        y = y,
        z = z,
        w = w
    )
}

internal fun VertexNormalLocal.mapToDomain(): VertexNormalData {
    return VertexNormalData(
        x = x,
        y = y,
        z = z,
        w = w
    )
}

internal fun FaceLocal.mapToDomain(): FaceData {
    return FaceData(
        vertexIndeces = vertexIndeces,
        vertexNormalIndeces = vertexNormalIndeces,
        color = color,
        materialName = materialName
    )
}