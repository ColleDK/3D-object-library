package com.colledk.colle_3d_object_library

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

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
                resourceId = R.raw.cube,
                name = "Cube",
                scale = 1,
                description = "This is a very cool description of the cube."
            ),
            ObjectDescription(
                resourceId = R.raw.human,
                name = "Human",
                scale = 5,
                description = "This is a very cool description of the human."
            ),
            ObjectDescription(
                resourceId = R.raw.minicooper,
                name = "Car",
                scale = 20,
                description = "This is a very cool description of the car."
            ),
            ObjectDescription(
                resourceId = R.raw.dragon,
                name = "Dragon",
                scale = 5,
                description = "This is a very cool description of the dragon."
            ),
            ObjectDescription(
                resourceId = R.raw.chair,
                name = "Chair",
                scale = 5,
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

}