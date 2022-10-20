package com.colledk.obj3d.gestures

import android.view.ScaleGestureDetector

class ZoomListener(private val nearVal: Float, private val farVal: Float, private val onZoom: (scale: Float) -> Unit) :
    ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private var mScaleFactor = farVal - nearVal

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        mScaleFactor *= detector.scaleFactor

        mScaleFactor = mScaleFactor.coerceIn(minimumValue = nearVal, maximumValue = farVal)

        onZoom(mScaleFactor)

        return true
    }
}