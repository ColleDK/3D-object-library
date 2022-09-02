package com.colledk.obj3d.view

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import com.colledk.obj3d.parser.ObjectFileParser

class ObjectSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: ObjectRenderer

    init {
        // We define the current OpenGL version to be 2.0
        setEGLContextClientVersion(2)

        renderer = ObjectRenderer()

        // We set the surface renderer to our custom renderer
        setRenderer(renderer)

        // We set the rendermode to dirty so we only update the view when needed
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    /**
     * Function for loading a 3D object from a raw resource
     * @param resourceId The resource id of the raw file containing the .obj file
     */
    fun loadObject(
        resourceId: Int
    ) {
        // Load in the data from the parser
        val data = ObjectFileParser().parseStream(context.resources.openRawResource(resourceId))

        // Set the data on the renderer
        renderer.data = data
        // Update the view with the new data
        requestRender()
    }

    /**
     * Function for setting the view's background color.
     * The function requires [Build.VERSION.SDK_INT] >= [Build.VERSION_CODES.O] otherwise it defaults to white background
     * @param color The color of the background as [Color]
     */
    fun setBackgroundColor(
        color: Color
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            renderer.backgroundColor = floatArrayOf(
                color.red(),
                color.green(),
                color.blue(),
                color.alpha()
            )
        } else {
            renderer.backgroundColor = floatArrayOf(
                1.0f,
                1.0f,
                1.0f,
                1.0f,
            )
        }
    }

    /**
     * Function for setting the view's background color
     * @param color The color of the background as [FloatArray].
     * The color array should be defined as (Red, Green, Blue, Alpha).
     * If no value is defined for a color value then it will be defaulted to the max value from the array
     */
    fun setBackgroundColor(
        color: FloatArray
    ) {
        when(color.size){
            0 -> {
                renderer.backgroundColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
            }
            1 -> {
                val maxValue = color.maxOf { it }
                renderer.backgroundColor = floatArrayOf(color[0], maxValue, maxValue, maxValue)
            }
            2 -> {
                val maxValue = color.maxOf { it }
                renderer.backgroundColor = floatArrayOf(color[0], color[1], maxValue, maxValue)
            }
            3 -> {
                val maxValue = color.maxOf { it }
                renderer.backgroundColor = floatArrayOf(color[0], color[1], color[2], maxValue)
            }
            else -> {
                renderer.backgroundColor = floatArrayOf(color[0], color[1], color[2], color[3])
            }
        }
    }
}