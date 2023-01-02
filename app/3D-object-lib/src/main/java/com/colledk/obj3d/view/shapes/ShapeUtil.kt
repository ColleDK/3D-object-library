package com.colledk.obj3d.view.shapes

import android.opengl.GLES20
import timber.log.Timber
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

fun getAttrLocation(mProgram: Int, name: String): Int {
    return GLES20.glGetAttribLocation(mProgram, name).also { checkForHandleError(it, name) }
}

fun getUnifLocation(mProgram: Int, name: String): Int {
    return GLES20.glGetUniformLocation(mProgram, name).also { checkForHandleError(it, name) }
}

internal fun checkForHandleError(handle: Int, name: String = ""){
    if (handle == -1){
        Timber.e("Error loading handle $name")
    }
}