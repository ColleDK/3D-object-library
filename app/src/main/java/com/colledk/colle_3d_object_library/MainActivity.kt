package com.colledk.colle_3d_object_library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
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
import com.colledk.obj3d.view.ObjectSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
                                loadObject(R.raw.building, scale = 15)
                                setBackgroundColor(floatArrayOf(0.6f, 1.0f, 1.0f, 1.0f))
                            }
                        }
                        glView!!
                    })

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
                        IntensityChooser(glView = glView)
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
                                        "Loading object $it"
                                    )
                                }
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun ColorChooser(glView: ObjectSurfaceView? = null, modifier: Modifier = Modifier) {
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
fun IntensityChooser(glView: ObjectSurfaceView? = null) {
    var sliderPosition by remember {
        mutableStateOf(1f)
    }

    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colors.onBackground, RoundedCornerShape(20.dp))
            .background(MaterialTheme.colors.background, RoundedCornerShape(20.dp))
            .padding(PaddingValues(horizontal = 15.dp, vertical = 5.dp))
    ) {
        Text(
            text = "Light intensity: " + "%.2f".format(sliderPosition),
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
    Slider(
        value = sliderPosition, onValueChange = {
            sliderPosition = it
            glView?.setLightIntensity(sliderPosition)
        },
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colors.onBackground,
            activeTrackColor = MaterialTheme.colors.onBackground,
            inactiveTrackColor = MaterialTheme.colors.background.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun ObjectChooser(
    scope: CoroutineScope,
    glView: ObjectSurfaceView? = null,
    modifier: Modifier = Modifier,
    loadingObjectCallback: (name: String) -> Unit = {}
) {
    val objects = mapOf(
        R.raw.building to "Building",
        R.raw.human to "Human",
        R.raw.minicooper to "Car",
        R.raw.streetlamp to "Lamp",
        R.raw.dragon to "Dragon",
    )

    val scales = listOf(
        15,
        5,
        40,
        4,
        5,
    )

    var objectIndex by remember {
        mutableStateOf(0)
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
                    objectIndex = (objectIndex - 1 + objects.size) % objects.size
                    loadingObjectCallback(objects.values.elementAt(objectIndex))
                    glView?.loadObject(
                        objects.keys.elementAt(objectIndex),
                        scales[objectIndex % scales.size]
                    )
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
            shape = RoundedCornerShape(20.dp)
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
                    objectIndex = (objectIndex + 1) % objects.size
                    loadingObjectCallback(objects.values.elementAt(objectIndex))
                    glView?.loadObject(
                        objects.keys.elementAt(objectIndex),
                        scales[objectIndex]
                    )
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
            shape = RoundedCornerShape(20.dp)
        ) {
            Image(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Right arrow"
            )
        }
    }
}