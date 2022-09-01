package com.colledk.obj3d

import android.content.Context
import android.opengl.GLSurfaceView
import com.colledk.obj3d.parser.ObjectFileParser

class ObjectSurfaceView(context: Context): GLSurfaceView(context) {

    fun initialize(
        resourceId: Int
    ){
        val data = ObjectFileParser().parseStream(context.resources.openRawResource(resourceId))


    }
}