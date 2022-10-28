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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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


            Scaffold(scaffoldState = scaffoldState) { padding ->
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

                    DescriptionDialog(shouldOpen = shouldOpen.value, currentDescription = descriptions.value[currentIndex.value].description) {
                        viewModel.setShouldOpenDialog(value = false)
                    }

                    ColorChooser(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .height(IntrinsicSize.Max)
                            .padding(PaddingValues(top = 15.dp)),
                        glView = glView
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(1f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bottom object chooser
                        ObjectChooser(
                            scope = scope,
                            modifier = Modifier
                                .fillMaxWidth(.7f)
                                .height(IntrinsicSize.Max)
                                .padding(PaddingValues(bottom = 15.dp)),
                            glView = glView,
                            loadingObjectCallback = {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        "Loading object $it\nPlease wait while it is loading!"
                                    )
                                }
                            },
                            onFinishLoading = {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        "Finished loading object $it"
                                    )
                                }
                            },
                            viewModel = viewModel,
                        )
                    }
                }

            }
        }
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

@Composable
fun DescriptionDialog(shouldOpen: Boolean, currentDescription: String, onDismiss: () -> Unit) {
    if (shouldOpen){
        Dialog(onDismissRequest = onDismiss ) {
            Text(text = currentDescription)
        }
    }
}

@Composable
fun ObjectChooser(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    viewModel: MainViewModel,
    glView: ObjectSurfaceView? = null,
    loadingObjectCallback: (name: String) -> Unit = {},
    onFinishLoading: (name: String) -> Unit = {}
) {

    var canSwitchObject by remember{
        mutableStateOf(true)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        // Left arrow button
        Button(
            onClick = {
                scope.launch {
                    canSwitchObject = false
                    viewModel.goToPreviousObject()
                    val currentObject = viewModel.descriptions.value[viewModel.currentObjectIndex.value]

                    loadingObjectCallback(currentObject.name)
                    glView?.loadObject(
                        resourceId = currentObject.resourceId,
                        objectName = currentObject.name
                    ) {
                        onFinishLoading(currentObject.name)
                        canSwitchObject = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxHeight()
                .border(
                    1.dp,
                    MaterialTheme.colors.onBackground,
                    RoundedCornerShape(20.dp)
                ),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
            shape = RoundedCornerShape(20.dp),
            enabled = canSwitchObject
        ) {
            Image(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Left arrow"
            )
        }

        // Change object text
        Box(
            Modifier
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onBackground,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(PaddingValues(horizontal = 15.dp))
        ) {
            Text(
                text = "Change object",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }

        // Right arrow button
        Button(
            onClick = {
                scope.launch {
                    canSwitchObject = false
                    viewModel.goToNextObject()

                    val currentObject = viewModel.descriptions.value[viewModel.currentObjectIndex.value]

                    loadingObjectCallback(currentObject.name)
                    glView?.loadObject(
                        resourceId = currentObject.resourceId,
                        objectName = currentObject.name
                    ) {
                        onFinishLoading(currentObject.name)
                        canSwitchObject = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxHeight()
                .border(
                    1.dp,
                    MaterialTheme.colors.onBackground,
                    RoundedCornerShape(20.dp)
                ),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
            shape = RoundedCornerShape(20.dp),
            enabled = canSwitchObject
        ) {
            Image(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Right arrow"
            )
        }
    }
}