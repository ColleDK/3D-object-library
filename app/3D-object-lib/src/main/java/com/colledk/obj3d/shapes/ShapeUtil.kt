package com.colledk.obj3d.shapes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

enum class ShapeUtil(val byteSize: Int) {
    SHORT(2),
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