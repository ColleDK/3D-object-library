package com.colledk.colle_3d_object_library

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(): ViewModel() {

    private val _shouldOpenDialog = MutableStateFlow<Boolean>(false)
    val shouldOpenDialog: StateFlow<Boolean> = _shouldOpenDialog.asStateFlow()

    private val _descriptions = MutableStateFlow<List<ObjectDescription>>(listOf())
    val descriptions: StateFlow<List<ObjectDescription>> = _descriptions.asStateFlow()

    private val _currentObjectIndex = MutableStateFlow<Int>(0)
    val currentObjectIndex: StateFlow<Int> = _currentObjectIndex.asStateFlow()

    init {
        _descriptions.value = listOf(
            ObjectDescription(
                resourceId = R.raw.minicooper,
                name = "Car",
                description = "This is a very cool description of the car."
            ),
            ObjectDescription(
                resourceId = R.raw.cube,
                name = "Cube",
                description = "This is a very cool description of the cube."
            ),
            ObjectDescription(
                resourceId = R.raw.human,
                name = "Human",
                description = "This is a very cool description of the human."
            ),
            ObjectDescription(
                resourceId = R.raw.dragon,
                name = "Dragon",
                description = "This is a very cool description of the dragon."
            ),
            ObjectDescription(
                resourceId = R.raw.chair,
                name = "Chair",
                description = "This is a very cool description of the chair."
            ),
        )
    }

    fun setShouldOpenDialog(value: Boolean){
        _shouldOpenDialog.value = value
    }

    fun goToPreviousObject() {
        _currentObjectIndex.value = (_currentObjectIndex.value - 1 + _descriptions.value.size) % _descriptions.value.size
    }

    fun goToNextObject() {
        _currentObjectIndex.value = (_currentObjectIndex.value + 1) % _descriptions.value.size
    }

    fun goToObject(index: Int) {
        _currentObjectIndex.value = index.coerceIn(0 until _descriptions.value.size)
    }

}