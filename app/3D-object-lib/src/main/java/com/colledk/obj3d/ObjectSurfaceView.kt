package com.colledk.obj3d

import android.content.Context
import android.opengl.GLSurfaceView
import com.colledk.obj3d.parser.ObjectFileParser

class ObjectSurfaceView(context: Context): GLSurfaceView(context) {

    /**
     * Function for loading a 3D object from a raw resource
     * @param resourceId The resource id of the raw file containing the .obj file
     */
    fun loadObject(
        resourceId: Int
    ){
        val data = ObjectFileParser().parseStream(context.resources.openRawResource(resourceId))


    }
}