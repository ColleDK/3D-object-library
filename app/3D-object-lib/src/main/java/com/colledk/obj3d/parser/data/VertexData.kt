package com.colledk.obj3d.parser.data

internal data class VertexData(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float? = null
){
    operator fun minus(other: VertexData): VertexData{
        return VertexData(
            x = this.x - other.x,
            y = this.y - other.y,
            z = this.z - other.z,
            w = this.w
        )
    }
}