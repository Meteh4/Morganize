package com.metoly.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun NoteBottomBar(
    onAddImage: () -> Unit,
    onDraw: () -> Unit,
    onToggleMarkdown: () -> Unit,
    onAddChecklistItem: () -> Unit,
    onSave: () -> Unit,
    saveContentDescription: String
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onAddImage) {
                Icon(Icons.Default.Image, contentDescription = "Add Image")
            }
            IconButton(onClick = onDraw) {
                Icon(Icons.Default.Brush, contentDescription = "Draw")
            }
            IconButton(onClick = onToggleMarkdown) {
                Icon(Icons.Default.FormatBold, contentDescription = "Toggle Markdown")
            }
            IconButton(onClick = onAddChecklistItem) {
                Icon(Icons.Default.Checklist, contentDescription = "Add Checklist Item")
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