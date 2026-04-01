package com.metoly.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun NoteBottomBar(
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    onAddChecklist: () -> Unit,
    onStartDrawing: () -> Unit,
    onSave: () -> Unit,
    saveContentDescription: String
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onAddText) {
                Icon(Icons.Default.TextFields, contentDescription = "Add Text")
            }
            IconButton(onClick = onAddImage) {
                Icon(Icons.Default.Image, contentDescription = "Add Image")
            }
            IconButton(onClick = onAddChecklist) {
                Icon(Icons.Default.Checklist, contentDescription = "Add Checklist")
            }
            IconButton(onClick = onStartDrawing) {
                Icon(Icons.Default.Create, contentDescription = "Start Drawing")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSave,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = saveContentDescription,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}