package com.colledk.obj3d.view

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.Matrix
import com.colledk.obj3d.allLet
import com.colledk.obj3d.math.MathUtil.getSignedTetrahedronVolume
import com.colledk.obj3d.math.MathUtil.hasSameSign
import com.colledk.obj3d.math.MathUtil.normalizeVector
import com.colledk.obj3d.math.MathUtil.toVertexData
import com.colledk.obj3d.parser.data.Material
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
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
    var data: ObjectData? = null

    @Volatile
    var backgroundColor: FloatArray = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    @Volatile
    var xAngle: Float = 0f

    @Volatile
    var yAngle: Float = 0f

    @Volatile
    var lightPosition: FloatArray = floatArrayOf(0f, 0f, 1f)

    @Volatile
    var cameraPosition: VertexData = VertexData(0f, 0f, 1f)

    // Define our matrices in a 4D spectrum (4x4)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val rotationX = FloatArray(16)
    private val rotationY = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var renderUpdate: RenderUpdate = RenderUpdate()
    private var shapes: Shape? = null
    private var materials: List<Material> = listOf()

    private var currentScreenHeight: Int = 0
    private var currentScreenWidth: Int = 0

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
        data?.let {
            shapes = Shape(
                objectData = it,
                materials = materials
            )
        }
    }

    override fun onDrawFrame(unused: GL10?) {
        // Update the background color
        if (renderUpdate.shouldUpdateColor) {
            GLES20.glClearColor(
                backgroundColor[0],
                backgroundColor[1],
                backgroundColor[2],
                backgroundColor[3]
            )
            renderUpdate = RenderUpdate(
                shouldUpdateColor = false,
                shouldUpdateShape = renderUpdate.shouldUpdateShape
            )
        }

        if (renderUpdate.shouldUpdateShape) {
            data?.let {
                shapes = Shape(
                    objectData = it,
                    materials = materials
                )
            }

            renderUpdate = RenderUpdate(
                shouldUpdateColor = renderUpdate.shouldUpdateColor,
                shouldUpdateShape = false
            )
        }

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
        val maxX = data?.vertices?.maxOf { it.x } ?: Float.MAX_VALUE
        val minX = data?.vertices?.minOf { it.x } ?: Float.MIN_VALUE
        val centerX = (abs(maxX) - abs(minX)) / 2
        val maxY = data?.vertices?.maxOf { it.y } ?: Float.MAX_VALUE
        val minY = data?.vertices?.minOf { it.y } ?: Float.MIN_VALUE
        val centerY = (abs(maxY) - abs(minY)) / 2
        val maxZ = data?.vertices?.maxOf { it.z } ?: Float.MAX_VALUE
        val minZ = data?.vertices?.minOf { it.z } ?: Float.MIN_VALUE
        val centerZ = (abs(maxZ) - abs(minZ)) / 2

        Matrix.setIdentityM(modelMatrix, 0)
        // Move the rotation point to the center
        Matrix.translateM(modelMatrix, 0, centerX, centerY, centerZ)
        // Rotate the object
        Matrix.multiplyMM(modelMatrix, 0, rotationX, 0, rotationY, 0)

        // Move the rotation point back to the start point
        Matrix.translateM(modelMatrix, 0, -centerX, -centerY, -centerZ)

        // Get the current mvp matrix
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        val inverseModelView = FloatArray(16)
        Matrix.invertM(inverseModelView, 0, mvMatrix, 0)

        val normalMatrix = FloatArray(16)
        Matrix.transposeM(normalMatrix, 0, inverseModelView, 0)

        // Draw the object
        shapes?.draw(
            modelMatrix = modelMatrix,
            viewMatrix = viewMatrix,
            projectionMatrix = projectionMatrix,
            normalMatrix = normalMatrix,
            lightPosition = lightPosition.normalizeVector(),
            cameraPosition = cameraPosition.toFloatArray()
        )
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        // We set the viewport
        GLES20.glViewport(0, 0, width, height)
        currentScreenHeight = height
        currentScreenWidth = width

        // We get the aspect ratio of the screen so we can transform the projectionmatrix to look correct on every screen
        val ratio = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 50f)
    }

    fun setObject(data: ObjectData) {
        this.data = data
        renderUpdate = RenderUpdate(
            shouldUpdateShape = true,
            shouldUpdateColor = renderUpdate.shouldUpdateColor
        )
    }

    fun setBackground() {
        renderUpdate = RenderUpdate(
            shouldUpdateColor = true,
            shouldUpdateShape = renderUpdate.shouldUpdateShape
        )
    }

    fun attachMaterials(materials: List<Material>) {
        this.materials = materials
        renderUpdate = RenderUpdate(
            shouldUpdateColor = renderUpdate.shouldUpdateColor,
            shouldUpdateShape = true
        )
    }

    fun attachMaterials(material: Material) {
        this.materials = listOf(material)
        renderUpdate = RenderUpdate(
            shouldUpdateColor = renderUpdate.shouldUpdateColor,
            shouldUpdateShape = true
        )
    }

    fun calculateRayPicking(mouseX: Float, mouseY: Float): Boolean {
        return unProject(mouseX = mouseX, mouseY = mouseY)
    }

    private fun unProject(mouseX: Float, mouseY: Float): Boolean {
        val nearPos = FloatArray(4)
        val farPos = FloatArray(4)
        val viewPort = intArrayOf(0, 0, currentScreenWidth, currentScreenHeight)

        // Use the built-in OpenGL unproject to get the positions on the direction vector from mouse click to back position
        val unProjectedNearPos = (GLU.gluUnProject(
            mouseX,
            mouseY,
            0f,
            mvMatrix,
            0,
            projectionMatrix,
            0,
            viewPort,
            0,
            nearPos,
            0
        )) == GLES20.GL_TRUE

        val unProjectedFarPos = (GLU.gluUnProject(
            mouseX,
            mouseY,
            1f,
            mvMatrix,
            0,
            projectionMatrix,
            0,
            viewPort,
            0,
            farPos,
            0
        )) == GLES20.GL_TRUE

        // If the unproject failed then we can't check for intersection
        if (unProjectedNearPos && unProjectedFarPos){
            // Convert the 4D coordinates to 3D coordinates by dividing with the w-component
            val near = floatArrayOf(nearPos[0] / nearPos[3], nearPos[1] / nearPos[3], nearPos[2] / nearPos[3])
            val far = floatArrayOf(farPos[0] / farPos[3], farPos[1] / farPos[3], farPos[2] / farPos[3])

            return checkRayPickingIntersection(q1 = near.toVertexData(), q2 = far.toVertexData())
        }
        return false.also { Timber.e("Failed to unproject coordinates for mouse click {$mouseX, $mouseY}\nReceived unprojected data: {$unProjectedNearPos, $unProjectedFarPos}") }
    }

    private fun checkRayPickingIntersection(q1: VertexData, q2: VertexData): Boolean{
        // Iterate each triangle of the object
        data?.faces?.forEach { face ->
            // The the current vertices
            val v1 = data?.vertices?.getOrNull(face.vertexIndeces[0])
            val v2 = data?.vertices?.getOrNull(face.vertexIndeces[1])
            val v3 = data?.vertices?.getOrNull(face.vertexIndeces[2])

            val (p1, p2, p3) = allLet(v1, v2, v3) {
                Timber.e("Cannot calculate ray picking intersection with null objects {$v1, $v2, $v3}")
                return false
            }

            val volume1 = getSignedTetrahedronVolume(a = q1, b = p1, c = p2, d = p3)
            val volume2 = getSignedTetrahedronVolume(a = q2, b = p1, c = p2, d = p3)

            when (hasSameSign(volume1, volume2)){
                true -> {
                    // If the volume has the same signs then the ray does not intersect with the face
                }
                false -> {
                    val volume3 = getSignedTetrahedronVolume(a = q1, b = q2, c = p1, d = p2)
                    val volume4 = getSignedTetrahedronVolume(a = q1, b = q2, c = p2, d = p3)
                    val volume5 = getSignedTetrahedronVolume(a = q1, b = q2, c = p3, d = p1)

                    if (hasSameSign(volume3, volume4, volume5)){
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun logMatrices() {
        Timber.d("Current view matrix: ${viewMatrix.joinToString()}")
        Timber.d("Current model matrix: ${modelMatrix.joinToString()}")
        Timber.d("Current projection matrix: ${projectionMatrix.joinToString()}")
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
                val error = GLES20.glGetShaderInfoLog(shader)
                Timber.e("Shader error $error")
            }
        }
    }
}