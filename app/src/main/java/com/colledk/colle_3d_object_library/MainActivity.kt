package com.colledk.colle_3d_object_library

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.colledk.colle_3d_object_library.components.AppBar
import com.colledk.colle_3d_object_library.components.SelectionDrawer
import com.colledk.obj3d.view.ObjectSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val shouldOpen = viewModel.shouldOpenDialog.collectAsState()
            val descriptions = viewModel.descriptions.collectAsState()
            val currentIndex = viewModel.currentObjectIndex.collectAsState()

            var glView by remember {
                mutableStateOf<ObjectSurfaceView?>(null)
            }

            val scope = rememberCoroutineScope()
            val scaffoldState = rememberScaffoldState()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            Scaffold(scaffoldState = scaffoldState, topBar = {
                AppBar(onButtonClicked = {
                    scope.launch {
                        if (drawerState.isOpen){
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                })
            }) { padding ->
                ModalDrawer(drawerState = drawerState, gesturesEnabled = drawerState.isOpen, drawerContent = {
                    SelectionDrawer(scope = scope, viewModel = viewModel, scaffoldState = scaffoldState, glView = glView)
                }) {
                    Box(modifier = Modifier.padding(paddingValues = padding)) {
                        AndroidView(factory = { ctx ->
                            glView = ObjectSurfaceView(ctx).apply {
                                scope.launch {
                                    loadMaterial(resourceId = R.raw.cubemtl)
                                    loadObject(
                                        resourceId = descriptions.value[currentIndex.value].resourceId,
                                        objectName = descriptions.value[currentIndex.value].name,
                                        overrideIfExists = false
                                    ) {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Loaded object ${descriptions.value[currentIndex.value].name}"
                                            )
                                        }
                                    }
                                    setObjectClickCallback { viewModel.setShouldOpenDialog(it) }
                                    setCameraPosition(x = 1f, y = 1f, z = 1f)
                                }
                            }
                            glView!!
                        })

                        DescriptionDialog(
                            shouldOpen = shouldOpen.value,
                            currentDescription = descriptions.value[currentIndex.value].description
                        ) {
                            viewModel.setShouldOpenDialog(value = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DescriptionDialog(shouldOpen: Boolean, currentDescription: String, onDismiss: () -> Unit) {
    if (shouldOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Text(text = currentDescription)
        }
    }
}