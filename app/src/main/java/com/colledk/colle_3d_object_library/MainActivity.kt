package com.colledk.colle_3d_object_library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import com.colledk.obj3d.ObjectSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var glView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = ObjectSurfaceView(this).apply {
            this.initialize(R.raw.cube)
        }

        setContentView(glView)
    }
}