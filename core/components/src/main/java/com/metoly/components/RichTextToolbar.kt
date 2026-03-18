package com.metoly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Rich-text formatting toolbar with toggle-mode buttons.
 *
 * Each button lights up (primary colour background) when its mode is active.
 * Pressing an active button deactivates it (toggle off).
 */
@Composable
fun RichTextToolbar(
    isBoldActive: Boolean,
    isItalicActive: Boolean,
    isBulletListActive: Boolean,
    isNumberedListActive: Boolean,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleBulletList: () -> Unit,
    onToggleNumberedList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RichToolbarButton(
                icon = { Icon(Icons.Default.FormatBold, contentDescription = "Kalın") },
                isActive = isBoldActive,
                onClick = onToggleBold
            )
            RichToolbarButton(
                icon = { Icon(Icons.Default.FormatItalic, contentDescription = "İtalik") },
                isActive = isItalicActive,
                onClick = onToggleItalic
            )
            RichToolbarButton(
                icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Madde listesi") },
                isActive = isBulletListActive,
                onClick = onToggleBulletList
            )
            RichToolbarButton(
                icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Numaralı liste") },
                isActive = isNumberedListActive,
                onClick = onToggleNumberedList
            )
        }
    }
}

@Composable
private fun RichToolbarButton(
    icon: @Composable () -> Unit,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
    ) {
        icon()
    }
}
