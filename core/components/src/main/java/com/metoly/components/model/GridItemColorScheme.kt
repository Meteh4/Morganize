package com.metoly.components.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Centralized color scheme for all grid item types (Text, Image, Checklist, etc.).
 * Every grid item MUST use these colors so they stay visually consistent.
 *
 * Provide via [LocalGridItemColors] or use [GridItemDefaults.colors] for the
 * Material-based default.
 */
@Immutable
data class GridItemColorScheme(
    /** Background of the item container. */
    val containerColor: Color,
    /** Primary text content color (title, body text). */
    val contentColor: Color,
    /** Placeholder / hint text color. */
    val placeholderColor: Color,
    /** Secondary content color (checked / dimmed text). */
    val dimmedContentColor: Color,
    /** Icon tint for subtle actions (delete, close, etc.). */
    val subtleIconColor: Color,
    /** Border color when the item is selected. */
    val selectedBorderColor: Color,
    /** Border color when the item is not selected. */
    val unselectedBorderColor: Color = Color.Transparent
)

/**
 * CompositionLocal to propagate [GridItemColorScheme] down the tree.
 * Falls back to a no-op scheme; always provide via [GridItemDefaults.colors].
 */
val LocalGridItemColors = staticCompositionLocalOf {
    GridItemColorScheme(
        containerColor = Color.Unspecified,
        contentColor = Color.Unspecified,
        placeholderColor = Color.Unspecified,
        dimmedContentColor = Color.Unspecified,
        subtleIconColor = Color.Unspecified,
        selectedBorderColor = Color.Unspecified,
        unselectedBorderColor = Color.Transparent
    )
}

object GridItemDefaults {
    /**
     * Returns a Material-theme-aware [GridItemColorScheme].
     * Call this inside a @Composable scope.
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        dimmedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        subtleIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unselectedBorderColor: Color = Color.Transparent
    ) = GridItemColorScheme(
        containerColor = containerColor,
        contentColor = contentColor,
        placeholderColor = placeholderColor,
        dimmedContentColor = dimmedContentColor,
        subtleIconColor = subtleIconColor,
        selectedBorderColor = selectedBorderColor,
        unselectedBorderColor = unselectedBorderColor
    )
}
