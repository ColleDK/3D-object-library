package com.colledk.colle_3d_object_library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import com.colledk.obj3d.ObjectSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var glView: ObjectSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = ObjectSurfaceView(this).apply {
            loadObject(R.raw.cube)
            setBackgroundColor(floatArrayOf(0.5f, 1.0f))
        }

        setContentView(glView)
    }
}