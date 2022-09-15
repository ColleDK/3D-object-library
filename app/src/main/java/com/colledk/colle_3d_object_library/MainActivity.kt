package com.colledk.colle_3d_object_library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.colledk.obj3d.view.ObjectSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var glView: ObjectSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = ObjectSurfaceView(this).apply {
            loadObject(R.raw.building, scale = 15)
            setBackgroundColor(floatArrayOf(0.6f, 1.0f, 1.0f, 1.0f))
        }

        setContentView(glView)
    }
}