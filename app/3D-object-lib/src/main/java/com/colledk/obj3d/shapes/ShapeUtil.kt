package com.colledk.obj3d.shapes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

enum class ShapeUtil(val byteSize: Int) {
    INT(4),
    FLOAT(4),
}

fun createFloatBuffer(array: () -> FloatArray): FloatBuffer {
    return ByteBuffer.allocateDirect(array().size * ShapeUtil.FLOAT.byteSize).run {
        order(ByteOrder.nativeOrder())

        asFloatBuffer().apply {
            put(array())

            position(0)
        }
    }
}

fun createIntBuffer(array: () -> IntArray): IntBuffer {
    return ByteBuffer.allocateDirect(array().size * ShapeUtil.INT.byteSize).run {
        order(ByteOrder.nativeOrder())

        asIntBuffer().apply {
            put(array())

            position(0)
        }
    }
}