package com.colledk.obj3d.parser

import com.colledk.obj3d.parser.data.FaceData
import com.colledk.obj3d.parser.data.ObjectData
import com.colledk.obj3d.parser.data.VertexData

internal object MockedParserData {

    val parsedData = ObjectData(
        vertices = listOf(
            VertexData(
                index = 1,
                x = 0.0f,
                y = 0.0f,
                z = 0.0f
            ),
            VertexData(
                index = 2,
                x = 0.0f,
                y = 0.0f,
                z = 1.0f
            ),
            VertexData(
                index = 3,
                x = 0.0f,
                y = 1.0f,
                z = 0.0f
            ),
            VertexData(
                index = 4,
                x = 0.0f,
                y = 1.0f,
                z = 1.0f
            ),
            VertexData(
                index = 5,
                x = 1.0f,
                y = 0.0f,
                z = 0.0f
            ),
            VertexData(
                index = 6,
                x = 1.0f,
                y = 0.0f,
                z = 1.0f
            ),
            VertexData(
                index = 7,
                x = 1.0f,
                y = 1.0f,
                z = 0.0f
            ),
            VertexData(
                index = 8,
                x = 1.0f,
                y = 1.0f,
                z = 1.0f
            ),
        ),
        faces = listOf(
            FaceData(
                vertexIndeces = listOf(
                    1, 7, 5
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 3, 7
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 4, 3
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 2, 4
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    3, 8, 7
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    3, 4, 8
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    5, 7, 8
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    5, 8, 6
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 5, 6
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    1, 6, 2
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    2, 6, 8
                )
            ),
            FaceData(
                vertexIndeces = listOf(
                    2, 8, 4
                )
            )
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