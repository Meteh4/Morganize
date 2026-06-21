package com.metoly.morganize.feature.list.components
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R

/**
 * A wrapper over [NoteCard] that implements Material 3 swipe-to-dismiss behavior.
 * - Swipe end-to-start → soft delete (with confirmation dialog)
 * - Swipe start-to-end → toggle pin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeToDismissNoteCard(
    note: Note,
    category: Category?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onPinToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> {
                showDeleteDialog = true
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.StartToEnd -> {
                onPinToggle()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            else -> {}
        }
    }

    if (showDeleteDialog) {
        com.metoly.components.DeleteNoteDialog(
            noteTitle = note.title,
            onConfirm = {
                showDeleteDialog = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart
                else
                    Alignment.CenterEnd
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            painterResource(id = com.metoly.morganize.core.ui.R.drawable.delete),
                            contentDescription = stringResource(R.string.feature_list_delete_note),
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = if (note.isPinned) stringResource(R.string.feature_list_unpin) else stringResource(R.string.feature_list_pin),
                            modifier = Modifier.padding(start = 20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    else -> {}
                }
            }
        },
        modifier = modifier
    ) {
        NoteCard(
            note = note,
            category = category,
            isSelected = isSelected,
            onClick = onClick,
            onPinToggle = onPinToggle
        )
    }
}
