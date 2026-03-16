package com.metoly.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/** Predefined Material-You-compatible note background colours (null = theme default). */
val NoteBackgroundColors: List<Color?> =
        listOf(
                null,
                Color(0xFFF28B82), // Red
                Color(0xFFFBBC04), // Yellow
                Color(0xFFFFF475), // Lemon
                Color(0xFFCCFF90), // Sage
                Color(0xFFA8DAB5), // Mint
                Color(0xFFCBF0F8), // Teal
                Color(0xFFAECBFA), // Blue
                Color(0xFFD7AEFA), // Purple
                Color(0xFFE6C9A8), // Sand
        )

/**
 * Horizontal scrollable row of colour swatches.
 *
 * @param selectedColor currently selected ARGB int; null means "use theme default"
 * @param onColorSelected callback with the ARGB value of the chosen colour, or null for default
 */
@Composable
fun ColorPickerRow(
        selectedColor: Int?,
        onColorSelected: (Int?) -> Unit,
        modifier: Modifier = Modifier
) {
    LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(NoteBackgroundColors) { color ->
            val isSelected =
                    when {
                        color == null && selectedColor == null -> true
                        color != null && selectedColor == color.toArgb() -> true
                        else -> false
                    }

            val borderWidth by
                    animateDpAsState(
                            targetValue = if (isSelected) 3.dp else 1.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "color_border"
                    )
            val borderColor by
                    animateColorAsState(
                            targetValue =
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            label = "color_border_color"
                    )

            Box(
                    modifier =
                            Modifier.size(36.dp)
                                    .clip(CircleShape)
                                    .background(color ?: MaterialTheme.colorScheme.surfaceVariant)
                                    .border(borderWidth, borderColor, CircleShape)
                                    .clickable { onColorSelected(color?.toArgb()) },
                    contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint =
                                    if (color == null) MaterialTheme.colorScheme.onSurfaceVariant
                                    else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
