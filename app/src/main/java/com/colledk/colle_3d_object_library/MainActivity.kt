package com.colledk.colle_3d_object_library

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.colledk.colle_3d_object_library.components.AppBar
import com.colledk.colle_3d_object_library.components.SelectionDrawer
import com.colledk.obj3d.view.ObjectSurfaceView
import kotlinx.coroutines.launch
import timber.log.Timber

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

            Scaffold(scaffoldState = scaffoldState, snackbarHost = { scaffoldState.snackbarHostState }, topBar = {
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
                Timber.d("New glView $glView")
                ModalDrawer(drawerState = drawerState, gesturesEnabled = false, drawerContent = {
                    glView?.let { // Need to wrap this with a let, because internally the glView would not be updated in the lambda functions.
                                  // This would mean the setBackgroundColor would never be called.
                        SelectionDrawer(scope = scope, viewModel = viewModel, scaffoldState = scaffoldState, glView = it, descriptions = descriptions.value)
                    }
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