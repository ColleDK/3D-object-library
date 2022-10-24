package com.colledk.obj3d.parser.data

data class Material(
    val name: String = "",
    var shininess: Float = 400f,
    var ambient: FloatArray = floatArrayOf(0f, 0f, 0f),
    var diffuse: FloatArray = floatArrayOf(1f, 1f, 1f),
    var specular: FloatArray = floatArrayOf(1f, 1f, 1f),
    var emissive: FloatArray = floatArrayOf(0f, 0f, 0f),
    var opticalDensity: Float = 1f,
    var opacity: Float = 1f,
    var illum: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Material

        if (name != other.name) return false
        if (shininess != other.shininess) return false
        if (!ambient.contentEquals(other.ambient)) return false
        if (!diffuse.contentEquals(other.diffuse)) return false
        if (!specular.contentEquals(other.specular)) return false
        if (!emissive.contentEquals(other.emissive)) return false
        if (opticalDensity != other.opticalDensity) return false
        if (opacity != other.opacity) return false
        if (illum != other.illum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + shininess.hashCode()
        result = 31 * result + ambient.contentHashCode()
        result = 31 * result + diffuse.contentHashCode()
        result = 31 * result + specular.contentHashCode()
        result = 31 * result + emissive.contentHashCode()
        result = 31 * result + opticalDensity.hashCode()
        result = 31 * result + opacity.hashCode()
        result = 31 * result + illum
        return result
    }
}
