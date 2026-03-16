package com.metoly.morganize.feature.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metoly.components.DeleteNoteDialog
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeToDismissNoteCard(
    note: Note,
    category: Category?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            showDeleteDialog = true
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    if (showDeleteDialog) {
        DeleteNoteDialog(
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.feature_list_delete_note),
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        modifier = modifier
    ) {
        NoteCard(
            note = note,
            category = category,
            isSelected = isSelected,
            onClick = onClick
        )
    }
}