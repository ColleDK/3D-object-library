package com.colledk.colle_3d_object_library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun SelectionDrawer(scope: CoroutineScope, viewModel: MainViewModel, glView: ObjectSurfaceView, descriptions: List<ObjectDescription>) {
    Column {
        Text(text = "Change background color", modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(bottom = 20.dp)
            ), textAlign = TextAlign.Center, style = TextStyle(fontWeight = FontWeight.Bold))
        ColorSelector { glView.setBackgroundColor(floatArrayOf(it.color.red, it.color.green, it.color.blue, it.color.alpha)) }
        Text(text = "Change painting color", modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(bottom = 20.dp)), textAlign = TextAlign.Center, style = TextStyle(fontWeight = FontWeight.Bold))
        ColorSelector { viewModel.setPainterColor(floatArrayOf(it.color.red, it.color.green, it.color.blue)) }
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