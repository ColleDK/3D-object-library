package com.colledk.obj3d.parser

import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData
import com.colledk.obj3d.parser.data.VertexNormalData

internal object MockedParserData {

    val parsedData = ObjectData(
        vertices = listOf(
            VertexData(
                x = 0.0f,
                y = 0.0f,
                z = 0.0f
            ),
            VertexData(
                x = 0.0f,
                y = 0.0f,
                z = 1.0f
            ),
            VertexData(
                x = 0.0f,
                y = 1.0f,
                z = 0.0f
            ),
            VertexData(
                x = 0.0f,
                y = 1.0f,
                z = 1.0f
            ),
            VertexData(
                x = 1.0f,
                y = 0.0f,
                z = 0.0f
            ),
            VertexData(
                x = 1.0f,
                y = 0.0f,
                z = 1.0f
            ),
            VertexData(
                x = 1.0f,
                y = 1.0f,
                z = 0.0f
            ),
            VertexData(
                x = 1.0f,
                y = 1.0f,
                z = 1.0f
            ),
        ),
        faces = listOf(
            FaceData(
                vertexIndeces = listOf(
                    0, 6, 4
                ),
                vertexNormalIndeces = listOf(
                    1, 1, 1
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    0, 2, 6
                ),
                vertexNormalIndeces = listOf(
                    1, 1, 1
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    0, 3, 2
                ),
                vertexNormalIndeces = listOf(
                    5, 5, 5
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    0, 1, 3
                ),
                vertexNormalIndeces = listOf(
                    5, 5, 5
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    2, 7, 6
                ),
                vertexNormalIndeces = listOf(
                    2, 2, 2
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    2, 3, 7
                ),
                vertexNormalIndeces = listOf(
                    2, 2, 2
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    4, 6, 7
                ),
                vertexNormalIndeces = listOf(
                    4, 4, 4
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    4, 7, 5
                ),
                vertexNormalIndeces = listOf(
                    4, 4, 4
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    0, 4, 5
                ),
                vertexNormalIndeces = listOf(
                    3, 3, 3
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    0, 5, 1
                ),
                vertexNormalIndeces = listOf(
                    3, 3, 3
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 5, 7
                ),
                vertexNormalIndeces = listOf(
                    0, 0, 0
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 7, 3
                ),
                vertexNormalIndeces = listOf(
                    0, 0, 0
                )
            )
        ),
        vertexNormals = listOf(
            VertexNormalData(
                0.0f,
                0.0f,
                1.0f,
            ),
            VertexNormalData(
                0.0f,
                0.0f,
                -1.0f,
            ),
            VertexNormalData(
                0.0f,
                1.0f,
                0.0f,
            ),
            VertexNormalData(
                0.0f,
                -1.0f,
                0.0f,
            ),
            VertexNormalData(
                1.0f,
                0.0f,
                0.0f,
            ),
            VertexNormalData(
                -1.0f,
                0.0f,
                0.0f,
            ),
        )
    )

    const val objectDataString = "# cube.obj\n" +
            "#\n" +
            "\n" +
            "g cube\n" +
            "\n" +
            "v  0.0  0.0  0.0\n" +
            "v  0.0  0.0  1.0\n" +
            "v  0.0  1.0  0.0\n" +
            "v  0.0  1.0  1.0\n" +
            "v  1.0  0.0  0.0\n" +
            "v  1.0  0.0  1.0\n" +
            "v  1.0  1.0  0.0\n" +
            "v  1.0  1.0  1.0\n" +
            "\n" +
            "vn  0.0  0.0  1.0\n" +
            "vn  0.0  0.0 -1.0\n" +
            "vn  0.0  1.0  0.0\n" +
            "vn  0.0 -1.0  0.0\n" +
            "vn  1.0  0.0  0.0\n" +
            "vn -1.0  0.0  0.0\n" +
            "\n" +
            "f  1//2  7//2  5//2\n" +
            "f  1//2  3//2  7//2\n" +
            "f  1//6  4//6  3//6\n" +
            "f  1//6  2//6  4//6\n" +
            "f  3//3  8//3  7//3\n" +
            "f  3//3  4//3  8//3\n" +
            "f  5//5  7//5  8//5\n" +
            "f  5//5  8//5  6//5\n" +
            "f  1//4  5//4  6//4\n" +
            "f  1//4  6//4  2//4\n" +
            "f  2//1  6//1  8//1\n" +
            "f  2//1  8//1  4//1"
}