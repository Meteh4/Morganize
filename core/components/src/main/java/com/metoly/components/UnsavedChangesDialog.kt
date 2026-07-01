package com.metoly.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UnsavedChangesDialog(
    onConfirmSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Unsaved Changes") },
        text = { Text(text = "You have unsaved changes. Do you want to save them before leaving?") },
        confirmButton = {
            TextButton(onClick = onConfirmSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Discard")
            }
        }
    )
}
