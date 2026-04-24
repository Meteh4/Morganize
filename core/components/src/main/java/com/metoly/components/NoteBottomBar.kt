package com.metoly.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Main bottom navigation bar for the note editor.
 * Provides quick actions to add various grid items (text, image, checklist, secret item),
 * toggle drawing mode, and save the current note.
 *
 * @param isDrawingMode Whether the drawing tool is currently active.
 * @param onAddText Callback to add a text item.
 * @param onAddImage Callback to add an image item.
 * @param onAddChecklist Callback to add a checklist item.
 * @param onAddSecretItem Callback to add a secret (locked) item.
 * @param onStartDrawing Callback to toggle drawing mode.
 * @param onSave Callback to save the note.
 * @param saveContentDescription Accessibility description for the save button.
 */
@Composable
fun NoteBottomBar(
    isDrawingMode: Boolean = false,
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    onAddChecklist: () -> Unit,
    onAddSecretItem: () -> Unit,
    onStartDrawing: () -> Unit,
    onSave: () -> Unit,
    saveContentDescription: String
) {
    val drawingContainerColor by animateColorAsState(
        targetValue = if (isDrawingMode)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = MorgAnimation.standard(),
        label = "draw_bg"
    )
    val drawingContentColor by animateColorAsState(
        targetValue = if (isDrawingMode)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = MorgAnimation.standard(),
        label = "draw_fg"
    )

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MorgDimens.spacingXxs,
        actions = {
            IconButton(onClick = onAddText) {
                Icon(
                    Icons.Default.TextFields,
                    contentDescription = "Add Text",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAddImage) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Add Image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAddChecklist) {
                Icon(
                    Icons.Default.Checklist,
                    contentDescription = "Add Checklist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAddSecretItem) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Lock,
                    contentDescription = "Add Secret Item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onStartDrawing,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = drawingContainerColor,
                    contentColor = drawingContentColor
                )
            ) {
                Icon(Icons.Default.Create, contentDescription = "Draw")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSave,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = saveContentDescription
                )
            }
        }
    )
}