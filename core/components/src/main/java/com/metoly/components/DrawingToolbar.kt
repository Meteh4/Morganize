// DrawingToolbar.kt
package com.metoly.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ────────────────────────────────────────────────────────────────────────────
// Pen color palette
// ────────────────────────────────────────────────────────────────────────────

private val PEN_COLORS: List<Long> = listOf(
    0xFF000000L, // Black
    0xFFFFFFFFL, // White
    0xFFEF5350L, // Red
    0xFFFF7043L, // Orange
    0xFFFFEE58L, // Yellow
    0xFF66BB6AL, // Green
    0xFF42A5F5L, // Blue
    0xFFAB47BCL, // Purple
    0xFF26C6DAL, // Cyan
    0xFFEC407AL, // Pink
)

// ────────────────────────────────────────────────────────────────────────────
// Slider ranges
// ────────────────────────────────────────────────────────────────────────────

private val STROKE_WIDTH_RANGE = 0.002f..0.04f   // canvas-fraction
private val ERASER_WIDTH_RANGE = 0.01f..0.12f    // canvas-fraction

/**
 * A professional drawing toolbar with:
 *  - Pen color palette
 *  - Stroke width slider
 *  - Eraser toggle + eraser size slider (shown only in eraser mode)
 *  - Undo (revert last stroke) button
 *  - Close / done button
 *
 * Single Responsibility: This composable owns only the *display* of drawing
 * controls and calls back to the parent for every state mutation.
 *
 * @param penColorArgb        Currently selected pen color as ARGB Long.
 * @param strokeWidthFraction Currently selected pen width (canvas fraction).
 * @param eraserWidthFraction Currently selected eraser width (canvas fraction).
 * @param isEraserMode        Whether the eraser tool is active.
 * @param canUndo             True when there is at least one stroke to undo.
 * @param onColorSelected     Called when the user taps a palette swatch.
 * @param onStrokeWidthChange Called as the user drags the stroke slider.
 * @param onEraserWidthChange Called as the user drags the eraser slider.
 * @param onToggleEraser      Called when the user taps the eraser / pen toggle.
 * @param onUndo              Called when the user taps the undo button.
 * @param onClose             Called when the user taps the done / close button.
 * @param modifier            Standard Compose modifier.
 */
@Composable
fun DrawingToolbar(
    penColorArgb: Long,
    strokeWidthFraction: Float,
    eraserWidthFraction: Float,
    isEraserMode: Boolean,
    canUndo: Boolean,
    onColorSelected: (Long) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onEraserWidthChange: (Float) -> Unit,
    onToggleEraser: () -> Unit,
    onUndo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEraserSlider by remember(isEraserMode) { mutableStateOf(isEraserMode) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // ── Row 1: color palette + tool actions ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Color swatches
                PEN_COLORS.forEach { colorArgb ->
                    ColorSwatch(
                        colorArgb = colorArgb,
                        isSelected = !isEraserMode && colorArgb == penColorArgb,
                        onClick = {
                            onColorSelected(colorArgb)
                            if (isEraserMode) onToggleEraser() // switch back to pen
                        }
                    )
                }

                DrawingToolbarDivider()

                // Eraser toggle
                DrawingToggleButton(
                    icon = if (isEraserMode) Icons.Default.AutoFixOff else Icons.Default.AutoFixNormal,
                    label = if (isEraserMode) "Switch to Pen" else "Eraser",
                    isActive = isEraserMode,
                    onClick = {
                        showEraserSlider = !isEraserMode
                        onToggleEraser()
                    }
                )

                DrawingToolbarDivider()

                // Undo / revert last stroke
                DrawingIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    enabled = canUndo,
                    onClick = onUndo
                )

                DrawingToolbarDivider()

                // Close drawing mode
                DrawingIconButton(
                    icon = Icons.Default.Close,
                    label = "Done",
                    enabled = true,
                    onClick = onClose
                )
            }

            // ── Row 2: stroke / eraser slider ────────────────────────────
            Spacer(Modifier.height(6.dp))

            if (isEraserMode) {
                LabeledSlider(
                    label = "Silgi",
                    value = eraserWidthFraction,
                    valueRange = ERASER_WIDTH_RANGE,
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    onValueChange = onEraserWidthChange
                )
            } else {
                LabeledSlider(
                    label = "Kalem",
                    value = strokeWidthFraction,
                    valueRange = STROKE_WIDTH_RANGE,
                    thumbColor = Color(penColorArgb.toULong().toLong()),
                    onValueChange = onStrokeWidthChange
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Private sub-components
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun ColorSwatch(
    colorArgb: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderWidth: Dp by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "swatch_border"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "swatch_border_color"
    )

    Box(
        modifier = Modifier
            .size(28.dp)
            .shadow(if (isSelected) 4.dp else 1.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(colorArgb.toULong().toLong()))
            .border(borderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun DrawingToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f)
        else
            Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "toggle_bg_$label"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "toggle_icon_$label"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DrawingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DrawingToolbarDivider() {
    VerticalDivider(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(24.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    thumbColor: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = thumbColor,
                activeTrackColor = thumbColor.copy(alpha = 0.7f)
            )
        )
    }
}
