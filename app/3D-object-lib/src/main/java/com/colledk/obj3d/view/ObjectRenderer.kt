package com.colledk.obj3d.view

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.shapes.Shape
import com.colledk.obj3d.shapes.ShapeUtil
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

internal class ObjectRenderer : GLSurfaceView.Renderer {
    @Volatile
    lateinit var data: ObjectData

    @Volatile
    var backgroundColor: FloatArray = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    @Volatile
    var xAngle: Float = 0f

    @Volatile
    var yAngle: Float = 0f

    // Define our matrices in a 4D spectrum (4x4)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    private val rotationX = FloatArray(16)
    private val rotationY = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private lateinit var shape: Shape

    override fun onSurfaceCreated(unsued: GL10?, config: EGLConfig?) {
        // We set the background color
        GLES20.glClearColor(
            backgroundColor[0],
            backgroundColor[1],
            backgroundColor[2],
            backgroundColor[3]
        )

        // Enable depth testing so that we don't display anything that is not viewable
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Create the objects from the objectdata
        shape = Shape(data)
    }

    override fun onDrawFrame(unused: GL10?) {
        val mvpMatrix = FloatArray(16)
        val mvMatrix = FloatArray(16)

        // On every frame we clear the color and depth buffer so we don't use any data from previous frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // We get the view matrix for the camera
        Matrix.setLookAtM(viewMatrix, 0, 5f, 2f, 5f, 0f, 0f, 0f, 0f, 1.0f, 0f)

        // Calculate the vp matrix
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Set the rotation of the object
        Matrix.setRotateM(rotationX, 0, xAngle, 0f, 1f, 0f)
        Matrix.setRotateM(rotationY, 0, yAngle, 1f, 0f, 0f)

        // Find the center point
        val maxX = data.vertices.maxOf { it.x }
        val minX = data.vertices.minOf { it.x }
        val centerX = (abs(maxX) - abs(minX)) / 2
        val maxY = data.vertices.maxOf { it.y }
        val minY = data.vertices.minOf { it.y }
        val centerY = (abs(maxY) - abs(minY)) / 2
        val maxZ = data.vertices.maxOf { it.z }
        val minZ = data.vertices.minOf { it.z }
        val centerZ = (abs(maxZ) - abs(minZ)) / 2

        Timber.d("Center coordinates are {$centerX, $centerY, $centerZ}")

        // Move the rotation point to the center
        Matrix.translateM(rotationMatrix, 0, centerX, centerY, centerZ)
        // Rotate the object
        Matrix.multiplyMM(rotationMatrix, 0, rotationX, 0, rotationY, 0)
        // Move the rotation point back to the start point
        Matrix.translateM(rotationMatrix, 0, -centerX, -centerY, -centerZ)

        // Get the current mvp matrix
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, rotationMatrix, 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, rotationMatrix, 0)

        // Draw the object
        val lightPosition = floatArrayOf(
            centerX,
            centerY,
            -5f
        )
        shape.draw(mvpMatrix = mvpMatrix, mvMatrix = mvMatrix, lightPosition = lightPosition)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        // We set the viewport
        GLES20.glViewport(0, 0, width, height)

        // We get the aspect ratio of the screen so we can transform the projectionmatrix to look correct on every screen
        val ratio = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 50f)
    }

}

internal fun loadShader(type: Int, shaderCode: String): Int {
    // Create the shader from the type
    return GLES20.glCreateShader(type).also { shader ->

        // Add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Create a buffer to retrieve the status of the compiled shader
        val shaderStatus: IntBuffer = ByteBuffer.allocateDirect(1 * ShapeUtil.INT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asIntBuffer().apply {
                position(0)
            }
        }

        // Retrieve the status of the shader
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, shaderStatus)

        when (shaderStatus.get()) {
            1 -> { /* Everything went okay so we don't need to do anything here */
            }
            else -> {
                // Something went wrong when compiling the shader
                Timber.e("Error loading shader $shaderCode")
            }
        }
    }
}