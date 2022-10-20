package com.colledk.obj3d.parser.data

import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

internal data class VertexData(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float? = null
) {
    internal operator fun minus(other: VertexData): VertexData {
        return VertexData(
            x = this.x - other.x,
            y = this.y - other.y,
            z = this.z - other.z,
            w = this.w?.minus(other.w ?: 0f)
        )
    }

    internal operator fun plus(other: VertexData): VertexData {
        return VertexData(
            x = this.x + other.x,
            y = this.y + other.y,
            z = this.z + other.z,
            w = this.w?.plus(other.w ?: 0f)
        )
    }

    internal operator fun times(other: VertexData): VertexData {
        return VertexData(
            x = this.x * other.x,
            y = this.y * other.y,
            z = this.z * other.z,
            w = this.w?.times(other.w ?: 1f)
        )
    }

    internal operator fun times(n: Float): VertexData {
        return VertexData(
            x = this.x * n,
            y = this.y * n,
            z = this.z * n,
            w = this.w?.times(n)
        )
    }

    internal operator fun div(other: VertexData): VertexData {
        return VertexData(
            x = this.x / other.x,
            y = this.y / other.y,
            z = this.z / other.z,
            w = this.w?.div(other.w ?: 1f)
        )
    }

    internal operator fun div(n: Float): VertexData {
        return VertexData(
            x = x / n,
            y = y / n,
            z = z / n,
            w = w?.div(n)
        )
    }

    internal fun crossProduct(other: VertexData): VertexData {
        return VertexData(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    internal fun dotProduct(other: VertexData): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    internal fun length(): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }

    internal fun normalize(): VertexData {
        return this / this.length()
    }

    internal fun angleBetween(other: VertexData): Double {
        val theta = this.dotProduct(other).toDouble() / (this.length() * other.length())

        return Math.toDegrees(
            acos(
                theta
            )
        )
    }

    internal fun toFloatArray(): FloatArray {
        w?.let {
            return floatArrayOf(x, y, z, it)
        } ?: run {
            return floatArrayOf(x, y, z)
        }
    }
}