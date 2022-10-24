package com.colledk.obj3d.math

import com.colledk.obj3d.parser.data.VertexData
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {
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

    // Check if all elements have the same sign (-/+)
    internal fun hasSameSign(vararg elements: Float): Boolean {
        val filteredElements = elements.filter { it < 0f }
        return filteredElements.isEmpty() || filteredElements.size == elements.size
    }
}