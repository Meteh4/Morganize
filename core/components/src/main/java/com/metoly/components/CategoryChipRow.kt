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
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * A horizontally scrollable row of [FilterChip]s for category selection/filtering.
 *
 * @param categories the list of available categories
 * @param selectedCategoryId the currently active filter (null = "All")
 * @param onCategorySelected invoked with the chosen category ID, or null for "All"
 * @param onAddCategory optional callback shown as an "Add" chip (pass null to hide)
 */
@Composable
fun CategoryChipRow(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddCategory: (() -> Unit)? = null
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MorgDimens.spacingSm),
        contentPadding = PaddingValues(horizontal = MorgDimens.screenPaddingHorizontal)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )
        }

        items(categories, key = { it.id }) { category ->
            val chipColor = Color(category.colorArgb)
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor.copy(alpha = 0.3f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
            )
        }

        if (onAddCategory != null) {
            item {
                FilterChip(
                    selected = false,
                    onClick = onAddCategory,
                    label = { Text("New") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.add),
                            contentDescription = "Add category"
                        )
                    }
                )
            }
        }
    }
}
