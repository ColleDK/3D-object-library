package com.colledk.obj3d.shapes

import android.opengl.GLES20
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.view.loadShader
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.max
import kotlin.math.min

internal class Shape(
    private val objectData: ObjectData
) {

    private val color = floatArrayOf(
        1.0f,
        1.0f,
        1.0f,
        1.0f,
    )

    // Transform the object data vertices to coordinates
    private val coords: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.vertices.forEach { vertex ->
            array.add(vertex.x)
            array.add(vertex.y)
            array.add(vertex.z)
            vertex.w?.let {
                array.add(it)
            }
        }

        array.toFloatArray()
    }

    // Transform the object data faces to draworder
    private val drawOrder: () -> ShortArray = {
        val array = mutableListOf<Short>()

        // Data retrieved from the file will be indexed from 1 to n but we index 0 to n-1
        // We will therefore need transform the data
        objectData.faces.forEach { face ->
            array.addAll(
                face.vertexIndeces.map { (it - 1).toShort() }
            )
        }

        array.toShortArray()
    }

    // Define our buffers for the coordinates and draworder
    private val drawBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder().size * ShapeUtil.SHORT.byteSize).run {
            // Use Android's built in ordering
            order(ByteOrder.nativeOrder())

            asShortBuffer().apply {
                // Insert the draworder into the buffer
                put(drawOrder())

                // Set the position
                position(0)
            }
        }

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(coords().size * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(coords())

                position(0)
            }
        }

    // Create the shader code
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 aPosition;" +
                "void main(){" +
                "   gl_Position = uMVPMatrix * aPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main(){" +
                "   gl_FragColor = vColor;" +
                "}"

    // Initialize the program and attach shaders
    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)

            GLES20.glLinkProgram(program)
        }
    }

    // Create handles for variables in the shaders
    private var aPositionHandle: Int = 0
    private var vColorHandle: Int = 0
    private var uMvpHandle: Int = 0

    private val vertexCount: Int = coords().size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX / ShapeUtil.FLOAT.byteSize

    // Create draw functionality
    fun draw(mvpMatrix: FloatArray){
        GLES20.glUseProgram(mProgram)

        // Load the color handle
        vColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // Set the color of the object
        GLES20.glUniform4fv(vColorHandle, 1, color, 0)

        // Apply projection matrix
        uMvpHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        GLES20.glUniformMatrix4fv(uMvpHandle, 1, false, mvpMatrix, 0)

        // Load position handle
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")


        // Prepare coordinate data
        GLES20.glVertexAttribPointer(
            aPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        // Enable handles
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder().size, GLES20.GL_UNSIGNED_SHORT, drawBuffer)

        // Disable handles
        GLES20.glDisableVertexAttribArray(aPositionHandle)
    }

    companion object{
        const val COORDS_PER_VERTEX = 3
    }
}