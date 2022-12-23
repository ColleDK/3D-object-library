package com.colledk.obj3d.math

import com.colledk.obj3d.parser.data.VertexData

internal object MockedMathData {
    val hasSameSignTest1 = floatArrayOf(1f, 1f, 1f, 1f)
    val hasSameSignTest2 = floatArrayOf(-5f, 1f, -1f, 1f)
    val hasSameSignTest3 = floatArrayOf(0f, 1f, 1f, 1f)
    val hasSameSignTest4 = floatArrayOf(-0f, 1f, 1f, 1f)
    val hasSameSignTest5 = floatArrayOf(.1f, 9999999f, 01231f, 1f)

    val tetrahedronA = VertexData(-1f, -1f, -2f)
    val tetrahedronB = VertexData(0f, 1f, 1f)
    val tetrahedronC = VertexData(1f, 2f, -3f)
}