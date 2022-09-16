package com.colledk.obj3d.shapes

import android.opengl.GLES20
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.view.loadShader
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

internal class Shape(
    private val objectData: ObjectData
) {

    private val color = floatArrayOf(
        0.7f,
        0.7f,
        0.7f,
        1.0f,
    )

    private val lightColor = floatArrayOf(
        1.0f,
        0.66f,
        0.0f,
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

    private val vertexNormals: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            face.vertexNormalIndeces?.let { indeces ->
                indeces.forEach { index ->
                    objectData.vertexNormals[index].let { normalData ->
                        array.add(normalData.x)
                        array.add(normalData.y)
                        array.add(normalData.z)
                        normalData.w?.let { w ->
                            array.add(w)
                        }
                    }
                }
            }
        }

        array.toFloatArray()
    }

    // Transform the object data faces to draworder
    private val drawOrder: () -> IntArray = {
        val array = mutableListOf<Int>()

        // Data retrieved from the file will be indexed from 1 to n but we index 0 to n-1
        // We will therefore need transform the data
        objectData.faces.forEach { face ->
            array.addAll(
                face.vertexIndeces
            )
        }

        array.toIntArray()
    }

    // Define our buffers for the coordinates and draworder
    private val drawBuffer: IntBuffer =
        ByteBuffer.allocateDirect(drawOrder().size * ShapeUtil.INT.byteSize).run {
            // Use Android's built in ordering
            order(ByteOrder.nativeOrder())

            asIntBuffer().apply {
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

    private val normalBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexNormals().size * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(vertexNormals())

                position(0)
            }
        }

    private val colorBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(objectData.vertices.size * 4 * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                val colors = mutableListOf<Float>()
                for (i in 0 until objectData.vertices.size){
                    colors.addAll(
                        color.toList()
                    )
                }
                put(colors.toFloatArray())

                position(0)
            }
        }

    private val lightColorBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(objectData.vertices.size * 4 * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                val colors = mutableListOf<Float>()
                for (i in 0 until objectData.vertices.size){
                    colors.addAll(
                        lightColor.toList()
                    )
                }

                put(colors.toFloatArray())

                position(0)
            }

        }

    // Create the shader code
    private val vertexShaderCode =
        "" +
                "uniform mat4 uMVPMatrix;" + // Model view projection matrix
                "uniform mat4 uMVMatrix;" + // Model view matrix used to compute the fragment position
                "" +
                "attribute vec4 aPosition;" + // Per-vertex position
                "attribute vec4 aColor;" + // Per-vertex color
                "attribute vec4 aLightColor;" + // Per-vertex light color
                "attribute vec3 aNormal;" + // The vertex normalized vector
                "" +
                "varying vec3 vPosition;" + // Position of the vertex
                "varying vec4 vColor;" + // Color of the fragment
                "varying vec4 vLightColor;" +
                "varying vec3 vNormal;" + // Color of the light for the fragment
                "" +
                "void main(){" +
                "   vPosition = vec3(uMVMatrix * aPosition);" +
                "   vColor = aColor;" +
                "   vLightColor = aLightColor;" +
                "   vNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));" +
                "   gl_PointSize = 10.0;" +
                "   gl_Position = uMVPMatrix * aPosition;" +
                "}"

    private val fragmentShaderCode =
        "" +
                "precision mediump float;" +
                "" +
                "uniform vec3 uLightPosition;" +
                "uniform float uLightIntensity;" +
                "" +
                "varying vec3 vPosition;" +
                "varying vec4 vColor;" +
                "varying vec4 vLightColor;" +
                "varying vec3 vNormal;" +
                "" +
                "void main(){" +
                "   float distance = length(uLightPosition - vPosition);" +
                "   vec3 lightVector = normalize(uLightPosition - vPosition);" +
                "   float diffuse = max(dot(vNormal, lightVector), 1.0);" +
                "   diffuse = diffuse * (uLightIntensity / (1.0 + (0.8 * distance * distance)));" +
                "   float ambientStrength = 0.1;" +
                "   vec4 ambient = ambientStrength * vLightColor;" +
                "   gl_FragColor = ambient * vColor + vColor * diffuse;" +
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
    private var aColorHandle: Int = 0
    private var aLightColorHandle: Int = 0
    private var aNormalHandle: Int = 0

    private var uMvpHandle: Int = 0
    private var uMvHandle: Int = 0
    private var uLightPosHandle: Int = 0
    private var uLightIntensityHandle: Int = 0

    private val vertexCount: Int = coords().size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * ShapeUtil.FLOAT.byteSize
    private val colorStride: Int = COORDS_PER_COLOR * ShapeUtil.FLOAT.byteSize

    // Create draw functionality
    fun draw(mvpMatrix: FloatArray, mvMatrix: FloatArray, lightPosition: FloatArray, lightIntensity: Float){
        GLES20.glUseProgram(mProgram)

        // Apply projection matrix
        uMvpHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        GLES20.glUniformMatrix4fv(uMvpHandle, 1, false, mvpMatrix, 0)

        uMvHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix")

        GLES20.glUniformMatrix4fv(uMvHandle, 1, false, mvMatrix, 0)

        uLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition")

        GLES20.glUniform3fv(uLightPosHandle, 1, lightPosition, 0)

        uLightIntensityHandle = GLES20.glGetUniformLocation(mProgram, "uLightIntensity")

        GLES20.glUniform1f(uLightIntensityHandle, lightIntensity * 5f)

        // Load the color handle
        aColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        if (aColorHandle == -1){
            Timber.e("Color handle error")
        }

        // Prepare color data
        GLES20.glVertexAttribPointer(
            aColorHandle,
            COORDS_PER_COLOR,
            GLES20.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )

        // Load position handle
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        if (aPositionHandle == -1){
            Timber.e("Position handle error")
        }

        // Prepare coordinate data
        GLES20.glVertexAttribPointer(
            aPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        // Load light color handle
        aLightColorHandle = GLES20.glGetAttribLocation(mProgram, "aLightColor")
        if (aLightColorHandle == -1){
            Timber.e("Light color handle error")
        }

        // Prepare light color data
        GLES20.glVertexAttribPointer(
            aLightColorHandle,
            COORDS_PER_COLOR,
            GLES20.GL_FLOAT,
            false,
            colorStride,
            lightColorBuffer
        )

        // Load vertex normal handle
        aNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal")
        if (aNormalHandle == -1){
            Timber.e("Vertex normal handle error")
        }

        // Prepare the vertex normal data
        GLES20.glVertexAttribPointer(
            aNormalHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )

        // Enable handles
        GLES20.glEnableVertexAttribArray(aColorHandle)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aLightColorHandle)

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder().size, GLES20.GL_UNSIGNED_INT, drawBuffer)

        // Disable handles
        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aColorHandle)
        GLES20.glEnableVertexAttribArray(aLightColorHandle)
    }

    companion object{
        const val COORDS_PER_VERTEX = 3
        const val COORDS_PER_COLOR = 4
    }
}