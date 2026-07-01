package com.metoly.morganize.feature.edit.components
import androidx.compose.runtime.getValue

import androidx.compose.ui.res.painterResource

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteTopBar
import com.metoly.morganize.feature.edit.R

@Composable
internal fun EditTopBar(
    onBack: () -> Unit,
    onDeleteClick: () -> Unit,
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    isSecretNote: Boolean,
    onToggleSecretNote: () -> Unit,
    hasReminder: Boolean,
    onReminderClick: () -> Unit,
    onShareClick: () -> Unit
) {
    NoteTopBar(
        title = stringResource(R.string.feature_edit_screen_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.feature_edit_back),
        selectedColor = selectedColor,
        onColorSelected = onColorSelected,
        isSecretNote = isSecretNote,
        onToggleSecretNote = onToggleSecretNote,
        extraActions = { resolvedColor ->
            val deleteIconColor by animateColorAsState(
                targetValue = if (resolvedColor != null) {
                    if (resolvedColor.luminance() > 0.5f) Color(0xFFB3261E)
                    else Color(0xFFF2B8B5)
                } else {
                    MaterialTheme.colorScheme.error
                },
                label = "topbar_delete"
            )
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share note"
                )
            }
            IconButton(onClick = onReminderClick) {
                androidx.compose.material3.Icon(
                    imageVector = if (hasReminder) androidx.compose.material.icons.Icons.Filled.Notifications else androidx.compose.material.icons.Icons.Outlined.Notifications,
                    contentDescription = "Set Reminder"
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.delete),
                    contentDescription = stringResource(R.string.feature_edit_delete_note),
                    tint = deleteIconColor
                )
            }
        }
    )
}
