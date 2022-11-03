package com.colledk.colle_3d_object_library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorSelector(onColorChanged: (ColorEnvelope) -> Unit = {}) {
    val controller = rememberColorPickerController()
    var currentPainterColor by remember {
        mutableStateOf(floatArrayOf(1f, 1f, 1f))
    }

    Column() {
        HsvColorPicker(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), controller = controller, onColorChanged = {
            onColorChanged(it)
            currentPainterColor = floatArrayOf(it.color.red, it.color.green, it.color.blue)
        })
//        Row() {
//            Text(text = "Current paint color ${currentPainterColor.joinToString()}")
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(
//                        Color(
//                            currentPainterColor[0],
//                            currentPainterColor[1],
//                            currentPainterColor[2]
//                        )
//                    )
//            )
//        }
    }

}