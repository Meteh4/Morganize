package com.metoly.components

import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.metoly.morganize.core.model.Tag
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * A horizontally scrollable row of [FilterChip]s for selecting multiple tags.
 *
 * @param tags the list of available tags
 * @param selectedTagIds the set of currently selected tag IDs
 * @param onTagToggled invoked with the tag ID that was tapped
 * @param onAddTag optional callback shown as an "Add" chip
 */
@Composable
fun TagChipRow(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagToggled: (Long) -> Unit,
    onAddTag: (() -> Unit)? = null
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MorgDimens.spacingSm),
        contentPadding = PaddingValues(horizontal = MorgDimens.screenPaddingHorizontal)
    ) {
        items(tags, key = { it.id }) { tag ->
            val chipColor = Color(tag.colorArgb)
            FilterChip(
                selected = selectedTagIds.contains(tag.id),
                onClick = { onTagToggled(tag.id) },
                label = { Text(tag.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.3f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        if (onAddTag != null) {
            item {
                FilterChip(
                    selected = false,
                    onClick = onAddTag,
                    label = { Text("New Tag") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.add),
                            contentDescription = "Add tag"
                        )
                    }
                )
            }
        }
    }
}
