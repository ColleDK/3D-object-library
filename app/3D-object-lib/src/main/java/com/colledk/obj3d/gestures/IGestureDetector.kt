package com.colledk.obj3d.gestures

import android.view.MotionEvent

interface IGestureDetector {
    fun handleMotionEvent(event: MotionEvent): Boolean
}