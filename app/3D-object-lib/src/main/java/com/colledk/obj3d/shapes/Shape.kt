package com.colledk.obj3d.shapes

import android.opengl.GLES20
import com.colledk.obj3d.math.MathUtil.crossProduct
import com.colledk.obj3d.parser.data.Material
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.view.loadShader
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

internal class Shape(
    private val objectData: ObjectData,
    private val materials: List<Material> = listOf()
) {

    // Transform the object data vertices to coordinates
    private val coords: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            face.vertexIndeces.forEach { index ->
                objectData.vertices[index].let {
                    array.add(it.x)
                    array.add(it.y)
                    array.add(it.z)
                }
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
                    }
                }
            } ?: run {
                // If the face has no attached vertex normal then we calculate it
                for (i in 0 until face.vertexIndeces.size){
                    val current = objectData.vertices[face.vertexIndeces[i]]
                    val prev = objectData.vertices[face.vertexIndeces[(i-1+face.vertexIndeces.size)%face.vertexIndeces.size]]
                    val next = objectData.vertices[face.vertexIndeces[(i+1)%face.vertexIndeces.size]]

                    val curPrev = prev - current
                    val curNext = next - current

                    val cross = curPrev.crossProduct(curNext)

                    array.add(cross.x)
                    array.add(cross.y)
                    array.add(cross.z)
                }
            }
        }

        array.toFloatArray()
    }

    // Transform the object data faces to draworder
    private val drawOrder: () -> IntArray = {
        val array = mutableListOf<Int>()

        array.addAll((0..objectData.faces.size * 3))

        array.toIntArray()
    }

    private val colors: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            array.addAll(face.color.toList())
            array.addAll(face.color.toList())
            array.addAll(face.color.toList())
        }

        array.toFloatArray()
    }

    private val diffuses: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            array.addAll((materials.firstOrNull { it.name == face.materialName } ?: Material()).diffuse.toList())
            array.addAll((materials.firstOrNull { it.name == face.materialName } ?: Material()).diffuse.toList())
            array.addAll((materials.firstOrNull { it.name == face.materialName } ?: Material()).diffuse.toList())
        }

        array.toFloatArray()
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
        ByteBuffer.allocateDirect(colors().size * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(colors())

                position(0)
            }
        }

    private val diffuseBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(diffuses().size * ShapeUtil.FLOAT.byteSize).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(diffuses())

                position(0)
            }
        }

    // Create the shader code
    private val vertexShaderCode =
        "" +
                "uniform mat4 uProjectionMatrix;" +
                "uniform mat4 uViewMatrix;" +
                "uniform mat4 uModelMatrix;" +
                "uniform mat4 uNormalMatrix;" +
                "" +
                "attribute vec3 aPosition;" +
                "attribute vec3 aNormal;" +
                "attribute vec3 aColor;" +
                "attribute vec3 aDiffuse;" +
                "" +
                "varying vec3 vNormal;" +
                "varying vec3 vColor;" +
                "varying vec3 vDiffuse;" +
                "" +
                "void main(){" +
                "   vDiffuse = aDiffuse;" +
                "   vColor = aColor;" +
                "   vNormal = (uNormalMatrix * vec4(aNormal, 0.0)).xyz;" +
                "   gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(aPosition, 1.0);" +
                "}"

    private val fragmentShaderCode =
        "" +
                "precision mediump float;" +
                "" +
                "uniform vec3 uLightPosition;" +
                "" +
                "varying vec3 vNormal;" +
                "varying vec3 vColor;" +
                "varying vec3 vDiffuse;" +
                "" +
                "bool isNan(float val);" +
                "" +
                "void main(){" +
                "   vec3 normal = normalize(vNormal);" +
                "   float light = dot(uLightPosition, normal) * .5 + .5;" +
                "   vec3 diffuse = vDiffuse * vColor;" +
                "   vec3 color = diffuse * light;" +
                "" +
                "   if(isNan(light)){" +
                "       color = vec3(1.0, 0.6, 0.0);" +
                "   }" +
                "   gl_FragColor = vec4(color, 1.0);" +
                "}" +
                "" +
                "bool isNan(float val)" +
                "{" +
                "  return (val <= 0.0 || 0.0 <= val) ? false : true;" +
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
    private var aNormalHandle: Int = 0
    private var aColorHandle: Int = 0
    private var aDiffuseHandle: Int = 0

    private var uLightPositionHandle: Int = 0
    private var uProjectionMatrixHandle: Int = 0
    private var uViewMatrixHandle: Int = 0
    private var uModelMatrixHandle: Int = 0
    private var uNormalMatrixHandle: Int = 0

    internal fun prepareHandles(){
        // Attribute handles
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition").also { checkForHandleError(it, "Position") }
        aNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal").also { checkForHandleError(it, "Normal") }
        aColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor").also { checkForHandleError(it, "Color") }
        aDiffuseHandle = GLES20.glGetAttribLocation(mProgram, "aDiffuse").also { checkForHandleError(it, "Diffuse") }

        // Uniform handles
        uLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition").also { checkForHandleError(it, "Light position") }
        uProjectionMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uProjectionMatrix").also { checkForHandleError(it, "Projection matrix") }
        uViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uViewMatrix").also { checkForHandleError(it, "View matrix") }
        uModelMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelMatrix").also { checkForHandleError(it, "Model matrix") }
        uNormalMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMatrix").also { checkForHandleError(it, "Normal matrix") }
    }

    internal fun checkForHandleError(handle: Int, name: String = ""){
        if (handle == -1){
            Timber.e("Error loading handle $name")
        }
    }

    private val vertexCount: Int = coords().size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * ShapeUtil.FLOAT.byteSize
    private val colorStride: Int = COORDS_PER_COLOR * ShapeUtil.FLOAT.byteSize

    // Create draw functionality
    fun draw(modelMatrix: FloatArray, viewMatrix: FloatArray, projectionMatrix: FloatArray, normalMatrix: FloatArray, lightPosition: FloatArray){
        GLES20.glUseProgram(mProgram)

        prepareHandles()

        // Set the projection matrix
        GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, projectionMatrix, 0)
        // Set the view matrix
        GLES20.glUniformMatrix4fv(uViewMatrixHandle, 1, false, viewMatrix, 0)
        // Set the model matrix
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)

        // Set the reverse light direction
        GLES20.glUniform3fv(uLightPositionHandle, 1, lightPosition, 0)

        // Set the normal matrix
        GLES20.glUniformMatrix4fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        // Enable the attribute arrays
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glEnableVertexAttribArray(aColorHandle)
        GLES20.glEnableVertexAttribArray(aDiffuseHandle)

        // Prepare position attributes
        GLES20.glVertexAttribPointer(
            aPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            aNormalHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            normalBuffer
        )

        GLES20.glVertexAttribPointer(
            aColorHandle,
            COORDS_PER_COLOR,
            GLES20.GL_FLOAT,
            false,
            0,
            colorBuffer
        )

        GLES20.glVertexAttribPointer(
            aDiffuseHandle,
            COORDS_PER_COLOR,
            GLES20.GL_FLOAT,
            false,
            0,
            diffuseBuffer
        )

        // Draw the object
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder().size, GLES20.GL_UNSIGNED_INT, drawBuffer)

        // Disable the attribute arrays
        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
        GLES20.glDisableVertexAttribArray(aColorHandle)
        GLES20.glDisableVertexAttribArray(aDiffuseHandle)
    }

    companion object{
        const val COORDS_PER_VERTEX = 3
        const val COORDS_PER_COLOR = 3
    }
}