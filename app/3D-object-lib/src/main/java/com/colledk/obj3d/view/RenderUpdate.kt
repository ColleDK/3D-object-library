package com.colledk.obj3d.view

data class RenderUpdate(
    val shouldUpdateShape: Boolean = false,
    val shouldUpdateShapeColor: Boolean = false,
    val shouldUpdateColor: Boolean = false
)
