package com.colledk.colle_3d_object_library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.colledk.colle_3d_object_library.MainViewModel
import com.colledk.colle_3d_object_library.ObjectDescription

@Composable
fun OtherObjectChooser(items: List<ObjectDescription>, onClick: (index: Int) -> Unit) {
    var isExpanded by remember {
        mutableStateOf(false)
    }

    var selectedIndex by remember {
        mutableStateOf(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text(text = "Select object: ${items[selectedIndex].name}", modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = true })
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            items.forEachIndexed { index, item ->
                DropDownMenuItem(item = item, onClick = {
                    isExpanded = false
                    selectedIndex = index
                    onClick(index)
                })
            }
        }
    }
}

@Composable
fun DropDownMenuItem(item: ObjectDescription, onClick: () -> Unit) {
    DropdownMenuItem(onClick = onClick) {
        Text(text = item.name, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}