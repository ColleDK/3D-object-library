package com.colledk.obj3d.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector

private const val TOUCH_SCALE_FACTOR = 180f / 1000f

internal class GestureDetector(
    private val context: Context,
    private val listener: GestureListener,
    private val nearFrustumVal: Float,
    private val farFrustumVal: Float
) : IGestureDetector {
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var currentTouchMode: TouchMode = TouchMode.NONE

    internal data class GestureListener(
        val onZoom: (scale: Float) -> Unit,
        val onRotate: (xVal: Float, yVal: Float) -> Unit,
        val onClick: (clickX: Float, clickY: Float) -> Unit
    )

    private val scaleDetector = ScaleGestureDetector(
        context,
        ZoomListener(
            nearVal = nearFrustumVal,
            farVal = farFrustumVal,
        ) { scale ->
            listener.onZoom(scale)
        }
    )

    override fun handleMotionEvent(event: MotionEvent): Boolean {
        // First we call the pinch gesture handler
        scaleDetector.onTouchEvent(event)

        // If there was no pinch action then we check for other user inputs
        if (!scaleDetector.isInProgress){
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    if (currentTouchMode != TouchMode.MULTITOUCH){
                        currentTouchMode = TouchMode.PRESS
                        previousX = event.x
                        previousY = event.y
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (currentTouchMode != TouchMode.MULTITOUCH){
                        // When the user moves their finger we calculate the offset from the point they touched and use it as the rotation
                        currentTouchMode = TouchMode.MOVE
                        val dx = event.x - previousX
                        val dy = event.y - previousY

                        listener.onRotate(dx * TOUCH_SCALE_FACTOR, dy * TOUCH_SCALE_FACTOR)

                        previousX = event.x
                        previousY = event.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    when(currentTouchMode){
                        TouchMode.PRESS -> {
                            listener.onClick(previousX, previousY)
                        }
                        else -> { /* Do nothing */ }
                    }
                    currentTouchMode = TouchMode.NONE
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // User adds a 2nd finger to the input
                    currentTouchMode = TouchMode.MULTITOUCH
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // Make sure the last input was multitouch (could be false positive)
                    if (currentTouchMode == TouchMode.MULTITOUCH){
                        currentTouchMode = TouchMode.NONE
                    }
                }
            }
        }
        // We handled the event so we return true
        return true
    }


    private enum class TouchMode {
        PRESS,
        MOVE,
        NONE,
        MULTITOUCH,
    }
}