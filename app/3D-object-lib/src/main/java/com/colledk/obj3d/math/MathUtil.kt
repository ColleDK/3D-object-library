package com.colledk.obj3d.math

import com.colledk.obj3d.parser.data.VertexData
import timber.log.Timber
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {

    internal fun FloatArray.normalizeVector(): FloatArray{
        check(this.size == 3){
            Timber.e("Cannot normalize vector with size $size")
            return this
        }

        val x = this[0]
        val y = this[1]
        val z = this[2]

        val length = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

        return floatArrayOf(
            x / length,
            y / length,
            z / length
        )
    }

    internal fun FloatArray.crossProduct(other: FloatArray): FloatArray{
        check(this.size == 3 && other.size == 3){
            Timber.e("Cannot calculate cross product with sizes ${this.size} & ${other.size}")
            return this
        }

        val cross = floatArrayOf(
            this[1] * other[2] - this[2] * other[1],
            this[2] * other[0] - this[0] * other[2],
            this[0] * other[1] - this[1] * other[0]
        )

        return cross
    }

    internal fun VertexData.crossProduct(other: VertexData): VertexData {
        return VertexData(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    internal fun VertexData.dotProduct(other: VertexData): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    internal fun VertexData.length(): Float {
        return sqrt(this.x.pow(2) + this.y.pow(2) + this.z.pow(2))
    }

    internal fun VertexData.angle(other: VertexData): Double {
        val theta = this.dotProduct(other).toDouble() / (this.length() * other.length())

        return Math.toDegrees(
            acos(
                theta
            )
        )
    }
}