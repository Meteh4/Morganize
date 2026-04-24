package com.metoly.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import com.metoly.components.common.MorgDestructiveButton
import com.metoly.components.common.MorgDialog
import com.metoly.components.common.MorgOutlinedButton

/**
 * Premium delete confirmation dialog.
 *
 * Uses [MorgDialog] with a destructive (error) colour scheme for the icon
 * and confirm button, replacing the stock [AlertDialog].
 */
@Composable
fun DeleteNoteDialog(
    noteTitle: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MorgDialog(
        icon = Icons.Default.Delete,
        title = "Delete Note",
        text = if (noteTitle.isNotBlank())
            "Are you sure you want to delete \"$noteTitle\"? This action cannot be undone."
        else
            "Are you sure you want to delete this note? This action cannot be undone.",
        confirmButton = {
            MorgDestructiveButton(
                text = "Delete",
                onClick = onConfirm
            )
        },
        dismissButton = {
            MorgOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        onDismiss = onDismiss
    )
}