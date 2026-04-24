// RichTextToolbar.kt
package com.metoly.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.core.model.grid.TextAlignment

private const val FONT_SIZE_MIN = 8f
private const val FONT_SIZE_MAX = 36f

private val LINE_HEIGHT_OPTIONS = listOf(1.2f, 1.4f, 1.8f)

/**
 * A toolbar providing rich text formatting capabilities.
 * 
 * Includes toggles for bold/italic, lists, alignment, font size, and line height.
 * All formatting changes are emitted back via callbacks.
 * 
 * @param state Current formatting state of the selected text block.
 * @param onToggleBold Callback when bold toggle is tapped.
 * @param onToggleItalic Callback when italic toggle is tapped.
 * @param onToggleBulletList Callback when bullet list toggle is tapped.
 * @param onToggleNumberedList Callback when numbered list toggle is tapped.
 * @param onFontSizeIncrease Callback when font size '+' is tapped.
 * @param onFontSizeDecrease Callback when font size '-' is tapped.
 * @param onTextAlignCycle Callback to cycle through text alignments.
 * @param onLineHeightCycle Callback to cycle through line heights.
 * @param modifier Standard Compose modifier for the toolbar surface.
 */
@Composable
fun RichTextToolbar(
    state: RichTextEditorState,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleBulletList: () -> Unit,
    onToggleNumberedList: () -> Unit,
    onFontSizeIncrease: () -> Unit,
    onFontSizeDecrease: () -> Unit,
    onTextAlignCycle: () -> Unit,
    onLineHeightCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormatToggleButton(
                icon = Icons.Default.FormatBold,
                label = "Bold",
                isActive = state.isFormatActive(SpanFormatType.BOLD),
                onClick = onToggleBold
            )
            FormatToggleButton(
                icon = Icons.Default.FormatItalic,
                label = "Italic",
                isActive = state.isFormatActive(SpanFormatType.ITALIC),
                onClick = onToggleItalic
            )

            ToolbarDivider()
            FormatToggleButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                label = "Bullet",
                isActive = state.isBulletListActive,
                onClick = onToggleBulletList
            )
            FormatToggleButton(
                icon = Icons.Default.FormatListNumbered,
                label = "Numbered",
                isActive = state.isNumberedListActive,
                onClick = onToggleNumberedList
            )

            ToolbarDivider()
            ToolbarIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                label = "Font -",
                enabled = state.fontSize > FONT_SIZE_MIN,
                onClick = onFontSizeDecrease
            )
            Text(
                text = "${state.fontSize.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            ToolbarIconButton(
                icon = Icons.Default.KeyboardArrowUp,
                label = "Font +",
                enabled = state.fontSize < FONT_SIZE_MAX,
                onClick = onFontSizeIncrease
            )

            ToolbarDivider()
            val alignIcon = when (state.textAlign) {
                TextAlignment.Center -> Icons.Default.FormatAlignCenter
                TextAlignment.End -> Icons.AutoMirrored.Filled.FormatAlignRight
                TextAlignment.Start -> Icons.AutoMirrored.Filled.FormatAlignLeft
            }
            ToolbarIconButton(
                icon = alignIcon,
                label = "Align",
                onClick = onTextAlignCycle
            )

            ToolbarDivider()
            FormatToggleButton(
                icon = Icons.Default.FormatSize,
                label = "Line height",
                isActive = state.lineHeight != 1.4f,
                onClick = onLineHeightCycle
            )
            Text(
                text = "${state.lineHeight}×",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun ToolbarDivider() {
    VerticalDivider(
        modifier = Modifier
            .padding(horizontal = MorgDimens.spacingXs)
            .height(MorgDimens.toolbarDividerHeight),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun FormatToggleButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        animationSpec = MorgAnimation.standard(),
        label = "bg_$label"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = MorgAnimation.standard(),
        label = "icon_$label"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(MorgDimens.toolbarButtonSize)
            .clip(MorgShapes.toolbarToggle)
            .background(bgColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(MorgDimens.toolbarIconSize)
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(MorgDimens.toolbarButtonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(MorgDimens.toolbarIconSize)
        )
    }
}
fun nextTextAlign(current: TextAlignment): TextAlignment = when (current) {
    TextAlignment.Start -> TextAlignment.Center
    TextAlignment.Center -> TextAlignment.End
    TextAlignment.End -> TextAlignment.Start
}

fun nextLineHeight(current: Float): Float {
    val idx = LINE_HEIGHT_OPTIONS.indexOfFirst { kotlin.math.abs(it - current) < 0.01f }
    return LINE_HEIGHT_OPTIONS[(idx + 1) % LINE_HEIGHT_OPTIONS.size]
}

fun clampedFontSize(current: Float, delta: Float): Float =
    (current + delta).coerceIn(FONT_SIZE_MIN, FONT_SIZE_MAX)