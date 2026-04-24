package com.metoly.morganize.feature.list.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.common.MorgEmptyState
import com.metoly.morganize.feature.list.R

/**
 * The placeholder displayed in the detail pane when no note is selected for viewing.
 */
@Composable
internal fun EmptyDetailPane(modifier: Modifier = Modifier) {
    MorgEmptyState(
        icon = Icons.Outlined.NoteAlt,
        title = stringResource(R.string.feature_list_select_note_to_view),
        modifier = modifier.fillMaxSize()
    )
}