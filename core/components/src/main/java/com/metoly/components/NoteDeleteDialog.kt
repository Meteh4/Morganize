package com.metoly.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DeleteNoteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    noteTitle: String? = null,
) {
    val resolvedTitle = when {
        noteTitle == null -> stringResource(R.string.core_components_delete_dialog_generic_message)
        noteTitle.isBlank() -> stringResource(R.string.core_components_untitled)
        else -> noteTitle
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = stringResource(R.string.core_components_delete_dialog_title))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.core_components_delete_dialog_message,
                    resolvedTitle
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.core_components_delete_dialog_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.core_components_delete_dialog_cancel))
            }
        }
    )
}