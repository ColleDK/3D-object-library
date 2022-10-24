package com.colledk.obj3d

// A let function for varied amount of elements
inline fun <T: Any> allLet(vararg elements: T?, closure: () -> Nothing): List<T> {
    return if (elements.all { it != null }) {
        elements.filterNotNull()
    } else {
        closure()
    }
}