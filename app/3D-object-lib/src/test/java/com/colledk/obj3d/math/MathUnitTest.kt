package com.colledk.obj3d.math

import com.colledk.obj3d.parser.model.VertexData
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MathUnitTest {

    @Test
    fun testSignedTetrahedronVolume() {
        val result = MathUtil.getSignedTetrahedronVolume(
            VertexData(0f, 0f, 0f),
            MockedMathData.tetrahedronA,
            MockedMathData.tetrahedronB,
            MockedMathData.tetrahedronC
        )
        assert(result == 1f)
    }

    @Test
    fun testHasSameSign() {
        val result1 = MathUtil.hasSameSign(*MockedMathData.hasSameSignTest1)
        val result2 = MathUtil.hasSameSign(*MockedMathData.hasSameSignTest2)
        val result3 = MathUtil.hasSameSign(*MockedMathData.hasSameSignTest3)
        val result4 = MathUtil.hasSameSign(*MockedMathData.hasSameSignTest4)
        val result5 = MathUtil.hasSameSign(*MockedMathData.hasSameSignTest5)

        assert(result1 && !result2 && result3 && result4 && result5)
    }
}