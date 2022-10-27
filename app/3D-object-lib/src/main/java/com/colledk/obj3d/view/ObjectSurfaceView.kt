package com.colledk.obj3d.view

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.MotionEvent
import androidx.room.Room
import com.colledk.obj3d.db.ObjectDatabase
import com.colledk.obj3d.db.mapToDomain
import com.colledk.obj3d.db.mapToLocal
import com.colledk.obj3d.gestures.GestureDetector
import com.colledk.obj3d.gestures.IGestureDetector
import com.colledk.obj3d.parser.MaterialFileParser
import com.colledk.obj3d.parser.ObjectFileParser
import com.colledk.obj3d.parser.data.VertexData
import timber.log.Timber

class ObjectSurfaceView(context: Context) : GLSurfaceView(context) {

    private lateinit var gestureDetector: IGestureDetector

    private val renderer: ObjectRenderer

    private var hitObjectCallback: (hitObject: Boolean) -> Unit = {}

    init {
        // We define the current OpenGL version to be 2.0
        setEGLContextClientVersion(2)

        renderer = ObjectRenderer()

        // We set the surface renderer to our custom renderer
        setRenderer(renderer)

        // We set the rendermode to dirty so we only update the view when needed
        renderMode = RENDERMODE_WHEN_DIRTY

        createGestureDetector()
    }

    private fun createGestureDetector() {
        gestureDetector =
            GestureDetector(context = context, listener = GestureDetector.GestureListener(
                onZoom = { scale ->
                    renderer.zoomVal = scale
                    renderObject()
                },
                onRotate = { xVal, yVal ->
                    renderer.xAngle += xVal
                    renderer.yAngle += yVal
                    renderObject()
                },
                onClick = { clickX, clickY ->
                    // The last user input was a single press so we look for ray intersection
                    val hit = renderer.calculateRayPicking(
                        mouseX = clickX - left,
                        mouseY = clickY - top,
                    )
                    hitObjectCallback(hit)
                    Timber.d("Hit object $hit")
                }
            ),
                nearFrustumVal = renderer.frustumNear,
                farFrustumVal = renderer.frustumFar
            )
    }

    /**
     * Function for loading a 3D object from a raw resource.
     * @param resourceId The resource id of the raw file containing the .obj file.
     * @param onFinish Callback for when the file has finished loading.
     */
    suspend fun loadObject(
        resourceId: Int,
        onFinish: () -> Unit = {},
    ) {
        // Load in the data from the parser
        val data = ObjectFileParser().parseStream(
            inputStream = context.resources.openRawResource(resourceId),
            onFinish = onFinish
        )

        // Set the data on the renderer
        renderer.setObject(data = data)
        // Update the view with the new data
        renderObject()
    }

    /**
     * Function for loading a 3D object from a url.
     * @param url The full path of the url that the file should be loaded from (ex https://localhost:8000/x.obj).
     * @param onFinish Callback for when the file has finished loading.
     */
    suspend fun loadObject(
        url: String,
        onFinish: () -> Unit = {},
    ) {

        // Load in the data from the parser
        val data = ObjectFileParser().parseURL(
            url = url,
            onFinish = onFinish
        )

        // Set the data on the renderer
        renderer.setObject(data = data)

        // Update the view with the new data
        renderObject()
    }

    /**
     * Function for attaching a material file to the current object.
     * @param resourceId The id of the raw resource.
     * @param onFinish Callback for when the file has finished loading.
     */
    suspend fun loadMaterial(
        resourceId: Int,
        onFinish: () -> Unit = {}
    ) {
        val materials = MaterialFileParser().parseStream(
            inputStream = context.resources.openRawResource(resourceId),
            onFinish = onFinish
        )

        renderer.attachMaterials(materials = materials)

        renderObject()
    }

    /**
     * Function for attaching a material file from a url to the current object.
     * @param url The full path of the url that the file should be loaded from (i.e. https://localhost:8000/x.mtl).
     * @param onFinish Callback for when the file has finished loading.
     */
    suspend fun loadMaterial(
        url: String,
        onFinish: () -> Unit = {}
    ) {
        val materials = MaterialFileParser().parseURL(
            url = url,
            onFinish = onFinish
        )

        renderer.attachMaterials(materials = materials)

        renderObject()
    }

    /**
     * Function for setting the view's background color.
     * The function requires [Build.VERSION.SDK_INT] >= [Build.VERSION_CODES.O] otherwise it defaults to white background.
     * @param color The color of the background as [Color].
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
        renderer.setBackground()
        renderObject()
    }

    /**
     * Function for setting the view's background color.
     * @param color The color of the background as [FloatArray].
     * The color array should be defined as (Red, Green, Blue, Alpha).
     * If no value is defined for a color value then it will be defaulted to the max value from the array.
     */
    fun setBackgroundColor(
        color: FloatArray
    ) {
        when (color.size) {
            0 -> {
                renderer.backgroundColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
            }
            else -> {
                val maxValue = color.maxOf { it }
                renderer.backgroundColor = floatArrayOf(
                    color.getOrNull(0) ?: maxValue,
                    color.getOrNull(1) ?: maxValue,
                    color.getOrNull(2) ?: maxValue,
                    color.getOrNull(3) ?: maxValue
                )
            }
        }
        renderer.setBackground()
        renderObject()
    }

    /**
     * Function for setting the position of the lighting. The default position for the light is at position {0, 0, 1}.
     * @param x x coordinate of the position as [Float].
     * @param y y coordinate of the position as [Float].
     * @param z z coordinate of the position as [Float].
     */
    fun setLightPosition(x: Float, y: Float, z: Float) {
        renderer.lightPosition = floatArrayOf(x, y, z)
        renderObject()
    }

    /**
     * Function for setting the position of the camera. The default position for the camera is at position {0, 0, 1}.
     * @param x x coordinate of the position as [Float].
     * @param y y coordinate of the position as [Float].
     * @param z z coordinate of the position as [Float].
     */
    fun setCameraPosition(x: Float, y: Float, z: Float) {
        renderer.cameraPosition = VertexData(x = x, y = y, z = z)
    }

    /**
     * Function for setting the frustum of the camera. The frustum defines a FOV cone from the near point until the far point.
     * These points are by default used for input functionality with zoom and click. The default frustum view is {1, 50}.
     * @param near The near point of the frustum.
     * @param far The far point of the frustum.
     */
    fun setCameraFrustum(near: Float = 1f, far: Float = 50f) {
        renderer.frustumNear = near
        renderer.frustumFar = far
        createGestureDetector()
        renderLayout()
    }

    /**
     * Function for overriding the gesture handler with a custom one. The default gesture will handle rotation, zoom and click actions.
     * @param detector The custom gesture handler.
     */
    fun setCustomGestureDetector(detector: IGestureDetector) {
        gestureDetector = detector
    }

    /**
     * Function for setting a callback that will be called whenever a click action happens on the screen, and will return a [Boolean] for whether an object was hit or not.
     * @param callback The callback function that should be called whenever a user click happens.
     */
    fun setObjectClickCallback(callback: (hitObject: Boolean) -> Unit) {
        hitObjectCallback = callback
    }

    /**
     * Function for setting the current mode for calculating the intersection between the object and user inputs.
     * The following 2 modes have been implemented and can be used.
     * [IntersectionMode.TETRAHEDRON_VOLUME] - Calculates the intersection by checking the signed volume between the user input and the triangles from the object.
     * [IntersectionMode.MOLLER_TRUMBORE] - Calculates the intersection by using the Möller-Trumbore fast intersection algorithm.
     * @param mode The current mode that the intersection should be calculated with.
     */
    fun setIntersectionCalculationMode(mode: IntersectionMode = IntersectionMode.MOLLER_TRUMBORE){
        renderer.intersectionMode = mode
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        gestureDetector.handleMotionEvent(event = event)

    private fun renderObject() {
        requestRender()
    }

    private fun renderLayout() {
        requestLayout()
    }

    enum class IntersectionMode{
        MOLLER_TRUMBORE,
        TETRAHEDRON_VOLUME
    }
}
