package com.metoly.morganize.feature.list.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.common.MorgEmptyState
import com.metoly.morganize.feature.list.R

/**
 * The placeholder displayed in the master list pane when the user has no notes or active filters yield no results.
 */
@Composable
internal fun EmptyListContent(modifier: Modifier = Modifier) {
    MorgEmptyState(
        icon = Icons.Outlined.NoteAlt,
        title = stringResource(R.string.feature_list_no_notes_yet),
        subtitle = stringResource(R.string.feature_list_tap_to_create_first_note),
        modifier = modifier
    )
}