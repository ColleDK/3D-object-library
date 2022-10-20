package com.colledk.obj3d.math

import com.colledk.obj3d.parser.data.VertexData
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {

    internal fun FloatArray.normalizeVector(): FloatArray{
        check(this.size >= 3){
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

    internal fun FloatArray.toVertexData(): VertexData {
        return VertexData(
            x = this.getOrNull(0) ?: 0f,
            y = this.getOrNull(1) ?: 0f,
            z = this.getOrNull(2) ?: 0f,
            w = this.getOrNull(3) ?: 0f
        )
    }

    internal fun getSignedTetrahedronVolume(a: VertexData, b: VertexData, c: VertexData, d: VertexData): Float {
        // Get the direction vectors
        val ba = b - a
        val ca = c - a
        val da = d - a

        // Calculate the scalar triple product
        val cross = ba.crossProduct(ca)
        val dot = cross.dotProduct(da)

        // Volume is 1/6th of the scalar triple product
        return dot / 6
    }

    internal fun hasSameSign(vararg elements: Float): Boolean {
        val filteredElements = elements.filter { it < 0f }
        return filteredElements.isEmpty() || filteredElements.size == elements.size
    }
}