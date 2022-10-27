package com.colledk.obj3d.shapes

import android.opengl.GLES20
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

    // Add the face vertex normals
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

    // Create data for each material value
    private val diffuses: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.addAll(currentMaterial.diffuse.toList())
            array.addAll(currentMaterial.diffuse.toList())
            array.addAll(currentMaterial.diffuse.toList())
        }

        array.toFloatArray()
    }

    private val ambients: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.addAll(currentMaterial.ambient.toList())
            array.addAll(currentMaterial.ambient.toList())
            array.addAll(currentMaterial.ambient.toList())
        }

        array.toFloatArray()
    }

    private val emissives: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.addAll(currentMaterial.emissive.toList())
            array.addAll(currentMaterial.emissive.toList())
            array.addAll(currentMaterial.emissive.toList())
        }

        array.toFloatArray()
    }

    private val speculars: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.addAll(currentMaterial.specular.toList())
            array.addAll(currentMaterial.specular.toList())
            array.addAll(currentMaterial.specular.toList())
        }

        array.toFloatArray()
    }

    private val shininess: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.add(currentMaterial.shininess)
            array.add(currentMaterial.shininess)
            array.add(currentMaterial.shininess)
        }

        array.toFloatArray()
    }

    private val opacities: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.faces.forEach { face ->
            val currentMaterial = materials.firstOrNull { it.name == face.materialName } ?: Material()
            array.add(currentMaterial.opacity)
            array.add(currentMaterial.opacity)
            array.add(currentMaterial.opacity)
        }

        array.toFloatArray()
    }

    // Define our buffers
    private val drawBuffer: IntBuffer = createIntBuffer(drawOrder)

    private val vertexBuffer: FloatBuffer = createFloatBuffer(coords)

    private val normalBuffer: FloatBuffer = createFloatBuffer(vertexNormals)

    private val colorBuffer: FloatBuffer = createFloatBuffer(colors)

    private val diffuseBuffer: FloatBuffer = createFloatBuffer(diffuses)

    private val ambientBuffer: FloatBuffer = createFloatBuffer(ambients)

    private val emissiveBuffer: FloatBuffer = createFloatBuffer(emissives)

    private val specularBuffer: FloatBuffer = createFloatBuffer(speculars)

    private val shininessBuffer: FloatBuffer = createFloatBuffer(shininess)

    private val opacityBuffer: FloatBuffer = createFloatBuffer(opacities)

    // Create the shader code
    private val vertexShaderCode =
        "" +    // Define the uniform data for the vertex
                "uniform mat4 uProjectionMatrix;" +
                "uniform mat4 uViewMatrix;" +
                "uniform mat4 uModelMatrix;" +
                "uniform mat4 uNormalMatrix;" +
                "uniform vec3 uCameraPosition;" +
                "" +
                // Define the non-material attributes
                "attribute vec3 aPosition;" +
                "attribute vec3 aNormal;" +
                "attribute vec3 aColor;" +
                "" +
                // Define the material attributes
                "attribute vec3 aDiffuse;" +
                "attribute vec3 aAmbient;" +
                "attribute vec3 aEmissive;" +
                "attribute vec3 aSpecular;" +
                "attribute float aShininess;" +
                "attribute float aOpacity;" +
                "" +
                // Define the non-material outputs
                "varying vec3 vNormal;" +
                "varying vec3 vColor;" +
                "varying vec3 vSurfaceToView;" +
                "" +
                // Define the material outputs
                "varying vec3 vDiffuse;" +
                "varying vec3 vAmbient;" +
                "varying vec3 vEmissive;" +
                "varying vec3 vSpecular;" +
                "varying float vShininess;" +
                "varying float vOpacity;" +
                "" +
                "void main(){" +
                "" +
                    // Set the material outputs
                "   vDiffuse = aDiffuse;" +
                "   vAmbient = aAmbient;" +
                "   vEmissive = aEmissive;" +
                "   vSpecular = aSpecular;" +
                "   vShininess = aShininess;" +
                "   vOpacity = aOpacity;" +
                "" +
                    // Set the color
                "   vColor = aColor;" +
                    // Calculate the vertex normal based on the world position
                "   vec4 worldPosition = uNormalMatrix * vec4(aPosition, 1.0);" +
                "   vSurfaceToView = uCameraPosition - worldPosition.xyz;" +
                "   vNormal = (uNormalMatrix * vec4(aNormal, 0.0)).xyz;" +
                    // Set the position of the vertex based on the transformations
                "   gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(aPosition, 1.0);" +
                "}"

    private val fragmentShaderCode =
        "" +
                // Set the precision to high to get better results
                "precision highp float;" +
                "" +
                // Define the uniform location of the light
                "uniform vec3 uLightPosition;" +
                "" +
                // Define the material outputs from vertex shader
                "varying vec3 vDiffuse;" +
                "varying vec3 vAmbient;" +
                "varying vec3 vEmissive;" +
                "varying vec3 vSpecular;" +
                "varying float vShininess;" +
                "varying float vOpacity;" +
                "" +
                // Define the non-material outputs from vertex shader
                "varying vec3 vNormal;" +
                "varying vec3 vColor;" +
                "varying vec3 vSurfaceToView;" +
                "" +
                "void main(){" +
                    // Normalize the vertex normal
                "   vec3 normal = normalize(vNormal);" +
                "" +
                    // Calculate the halfvector between the light position and the surface
                "   vec3 surfaceToViewDirection = normalize(vSurfaceToView);" +
                "   vec3 halfVector = normalize(uLightPosition + surfaceToViewDirection);" +
                "" +
                    // Calculate the amount of light reflected on the surface
                "   float light = dot(uLightPosition, normal) * .5 + .5;" +
                "   float specularLight = clamp(dot(normal, halfVector), 0.0, 1.0);" +
                "" +
                "   vec3 effectiveDiffuse = vDiffuse * vColor;" +
                "" +
                    // Set the color of the pixel based on the material and light
                "   gl_FragColor = vec4(vEmissive + vAmbient * vec3(1.0, 1.0, 1.0) + effectiveDiffuse * light + vSpecular * pow(specularLight, vShininess), vOpacity);" +
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
    private var aAmbientHandle: Int = 0
    private var aEmissiveHandle: Int = 0
    private var aSpecularHandle: Int = 0
    private var aShininessHandle: Int = 0
    private var aOpacityHandle: Int = 0

    private var uLightPositionHandle: Int = 0
    private var uProjectionMatrixHandle: Int = 0
    private var uViewMatrixHandle: Int = 0
    private var uModelMatrixHandle: Int = 0
    private var uNormalMatrixHandle: Int = 0
    private var uCameraPositionHandle: Int = 0

    internal fun prepareHandles(){
        // Attribute handles
        aPositionHandle = getAttrLocation(mProgram, "aPosition")
        aNormalHandle = getAttrLocation(mProgram, "aNormal")
        aColorHandle = getAttrLocation(mProgram, "aColor")

        // Uniform handles
        aDiffuseHandle = getAttrLocation(mProgram, "aDiffuse")
        aAmbientHandle = getAttrLocation(mProgram, "aAmbient")
        aEmissiveHandle = getAttrLocation(mProgram, "aEmissive")
        aSpecularHandle = getAttrLocation(mProgram, "aSpecular")
        aShininessHandle = getAttrLocation(mProgram, "aShininess")
        aOpacityHandle = getAttrLocation(mProgram, "aOpacity")

        uLightPositionHandle = getUnifLocation(mProgram, "uLightPosition")
        uProjectionMatrixHandle = getUnifLocation(mProgram, "uProjectionMatrix")
        uViewMatrixHandle = getUnifLocation(mProgram, "uViewMatrix")
        uModelMatrixHandle = getUnifLocation(mProgram, "uModelMatrix")
        uNormalMatrixHandle = getUnifLocation(mProgram, "uNormalMatrix")
        uCameraPositionHandle = getUnifLocation(mProgram, "uCameraPosition")
    }

    private fun enableVertexArrays(){
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glEnableVertexAttribArray(aColorHandle)
        GLES20.glEnableVertexAttribArray(aDiffuseHandle)
        GLES20.glEnableVertexAttribArray(aAmbientHandle)
        GLES20.glEnableVertexAttribArray(aEmissiveHandle)
        GLES20.glEnableVertexAttribArray(aSpecularHandle)
        GLES20.glEnableVertexAttribArray(aShininessHandle)
        GLES20.glEnableVertexAttribArray(aOpacityHandle)
    }

    private fun disableVertexArrays(){
        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
        GLES20.glDisableVertexAttribArray(aColorHandle)
        GLES20.glDisableVertexAttribArray(aDiffuseHandle)
        GLES20.glDisableVertexAttribArray(aAmbientHandle)
        GLES20.glDisableVertexAttribArray(aEmissiveHandle)
        GLES20.glDisableVertexAttribArray(aSpecularHandle)
        GLES20.glDisableVertexAttribArray(aShininessHandle)
        GLES20.glDisableVertexAttribArray(aOpacityHandle)
    }

    private fun prepareFloatAttrPointer(handle: Int, coordsPerInput: Int, buffer: FloatBuffer){
        GLES20.glVertexAttribPointer(
            handle,
            coordsPerInput,
            GLES20.GL_FLOAT,
            false,
            0,
            buffer
        )
    }

    // Create draw functionality
    fun draw(modelMatrix: FloatArray, viewMatrix: FloatArray, projectionMatrix: FloatArray, normalMatrix: FloatArray, lightPosition: FloatArray, cameraPosition: FloatArray){
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
        GLES20.glUniform3fv(uCameraPositionHandle, 1, cameraPosition, 0)

        // Set the normal matrix
        GLES20.glUniformMatrix4fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        // Enable the attribute arrays
        enableVertexArrays()

        // Prepare position attributes
        prepareFloatAttrPointer(aPositionHandle, COORDS_PER_VERTEX, vertexBuffer)
        prepareFloatAttrPointer(aNormalHandle, COORDS_PER_VERTEX, normalBuffer)
        prepareFloatAttrPointer(aColorHandle, COORDS_PER_COLOR, colorBuffer)
        prepareFloatAttrPointer(aDiffuseHandle, COORDS_PER_VERTEX, diffuseBuffer)
        prepareFloatAttrPointer(aAmbientHandle, COORDS_PER_VERTEX, ambientBuffer)
        prepareFloatAttrPointer(aEmissiveHandle, COORDS_PER_VERTEX, emissiveBuffer)
        prepareFloatAttrPointer(aSpecularHandle, COORDS_PER_VERTEX, specularBuffer)
        prepareFloatAttrPointer(aShininessHandle, COORDS_PER_MATERIAL, shininessBuffer)
        prepareFloatAttrPointer(aOpacityHandle, COORDS_PER_MATERIAL, opacityBuffer)

        // Draw the object
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder().size, GLES20.GL_UNSIGNED_INT, drawBuffer)

        // Disable the attribute arrays
        disableVertexArrays()
    }

    companion object{
        const val COORDS_PER_VERTEX = 3
        const val COORDS_PER_COLOR = 3
        const val COORDS_PER_MATERIAL = 1
    }
}