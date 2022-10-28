package com.colledk.obj3d.parser.data

internal data class FaceData(
    val vertexIndeces: List<Int>,
    val vertexNormalIndeces: List<Int>? = null,
    val color: FloatArray = floatArrayOf(0.7f, 0.7f, 0.7f),
    val materialName: String = "",
    val objectGroupName: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceData

        if (vertexIndeces != other.vertexIndeces) return false
        if (vertexNormalIndeces != other.vertexNormalIndeces) return false
        if (!color.contentEquals(other.color)) return false
        if (materialName != other.materialName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertexIndeces.hashCode()
        result = 31 * result + (vertexNormalIndeces?.hashCode() ?: 0)
        result = 31 * result + color.contentHashCode()
        result = 31 * result + materialName.hashCode()
        return result
    }
}
