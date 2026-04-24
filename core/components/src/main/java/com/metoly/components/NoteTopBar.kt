package com.metoly.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Shared top bar for note screens.
 *
 * @param extraActions  Optional slot — receives [resolvedColor] so callers can
 *                      derive icon tints without duplicating color logic.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteTopBar(
    title: String,
    onBack: () -> Unit,
    backContentDescription: String,
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    isSecretNote: Boolean? = null,
    onToggleSecretNote: (() -> Unit)? = null,
    extraActions: @Composable RowScope.(resolvedColor: Color?) -> Unit = {}
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val resolvedColor = resolveNoteColor(selectedColor)

    val containerColor by animateColorAsState(
        targetValue = resolvedColor ?: MaterialTheme.colorScheme.surface,
        label = "topbar_container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (resolvedColor != null) {
            if (resolvedColor.luminance() > 0.5f) Color(0xDD000000) else Color(0xE6FFFFFF)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "topbar_content"
    )

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showColorPicker = !showColorPicker }) {
                    Icon(Icons.Default.Palette, contentDescription = "Pick Color")
                }
                DropdownMenu(
                    expanded = showColorPicker,
                    onDismissRequest = { showColorPicker = false }
                ) {
                    NoteColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { color ->
                            onColorSelected(color)
                            showColorPicker = false
                        }
                    )
                }
            }
            if (isSecretNote != null && onToggleSecretNote != null) {
                IconButton(onClick = onToggleSecretNote) {
                    Icon(
                        imageVector = if (isSecretNote) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Secret Note"
                    )
                }
            }
            extraActions(resolvedColor)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteColorPicker(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        ColorSwatch(
            themeColor = null,
            isSelected = selectedColor == null,
            onClick = { onColorSelected(null) }
        )
        NoteThemeColors.forEach { themeColor ->
            ColorSwatch(
                themeColor = themeColor,
                isSelected = selectedColor == themeColor.argb,
                onClick = { onColorSelected(themeColor.argb) }
            )
        }
    }
}


@Composable
internal fun ColorSwatch(
    themeColor: NoteThemeColor?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val displayColor = when {
        themeColor == null -> MaterialTheme.colorScheme.surfaceVariant
        isDark             -> themeColor.dark
        else               -> themeColor.light
    }

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "swatch_border_w"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        label = "swatch_border_c"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(displayColor)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (displayColor.luminance() > 0.5f)
                    Color.Black.copy(alpha = 0.6f)
                else
                    Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}