package com.colledk.colle_3d_object_library.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppBar(title: String = "3D-object library", icon: ImageVector = Icons.Filled.Menu, onButtonClicked: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = onButtonClicked ) {
                Icon(imageVector = icon, contentDescription = "")
            }
        }
    )

}