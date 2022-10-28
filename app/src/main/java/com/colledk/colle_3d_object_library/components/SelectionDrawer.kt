package com.colledk.colle_3d_object_library.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.colledk.colle_3d_object_library.MainViewModel
import com.colledk.colle_3d_object_library.ObjectDescription
import com.colledk.obj3d.view.ObjectSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SelectionDrawer(scope: CoroutineScope, viewModel: MainViewModel, scaffoldState: ScaffoldState, glView: ObjectSurfaceView, descriptions: List<ObjectDescription>) {
    Column {
        Text(text = "Change background color", modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(bottom = 20.dp)
            ), textAlign = TextAlign.Center, style = TextStyle(fontWeight = FontWeight.Bold))
        ColorSelector { color ->
            glView.setBackgroundColor(floatArrayOf(color.color.red, color.color.green, color.color.blue, color.color.alpha))
        }
        Text(text = "Change painting color", modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(bottom = 20.dp)), textAlign = TextAlign.Center, style = TextStyle(fontWeight = FontWeight.Bold))
        ColorSelector { color ->
            scope.launch {
                glView.setObjectColor(floatArrayOf(color.color.red, color.color.green, color.color.blue))
            }
        }
        Spacer(modifier = Modifier.fillMaxHeight().weight(1f))
        OtherObjectChooser(items = descriptions, onClick = {
            viewModel.goToObject(index = it)
            scope.launch {
                val currentObject = descriptions[viewModel.currentObjectIndex.value]
                glView.loadObject(resourceId = currentObject.resourceId, objectName = currentObject.name)
            }
        })
    }
}


@Composable
fun ColorChooser(
    modifier: Modifier = Modifier,
    glView: ObjectSurfaceView? = null,
) {
    val colorNames = mapOf(
        floatArrayOf(0.6f, 1.0f, 1.0f, 1.0f) to "Cyan",
        floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f) to "Red",
        floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f) to "Green",
        floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f) to "White",
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        colorNames.toList().forEach { (color, colorName) ->
            Button(
                onClick = { glView?.setBackgroundColor(color) },
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        1.dp,
                        MaterialTheme.colors.onBackground,
                        RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = colorName)
            }
        }
    }
}