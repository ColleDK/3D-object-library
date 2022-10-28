package com.colledk.colle_3d_object_library.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorSelector(onColorChanged: (ColorEnvelope) -> Unit = {}) {
    val controller = rememberColorPickerController()
    HsvColorPicker(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp), controller = controller, onColorChanged = onColorChanged)

}