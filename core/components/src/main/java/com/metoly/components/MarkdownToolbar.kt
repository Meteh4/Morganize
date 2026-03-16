package com.metoly.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A floating toolbar that provides basic Markdown formatting actions.
 *
 * @param onBold insert **bold** markup
 * @param onItalic insert *italic* markup
 * @param onHeading insert # heading markup
 * @param onBulletList insert - list item markup
 * @param onNumberedList insert 1. numbered item markup
 */
@Composable
fun MarkdownToolbar(
        onBold: () -> Unit,
        onItalic: () -> Unit,
        onHeading: () -> Unit,
        onBulletList: () -> Unit,
        onNumberedList: () -> Unit,
        modifier: Modifier = Modifier
) {
    Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBold) {
                Icon(imageVector = Icons.Default.FormatBold, contentDescription = "Kalın")
            }
            IconButton(onClick = onItalic) {
                Icon(imageVector = Icons.Default.FormatItalic, contentDescription = "İtalik")
            }
            IconButton(onClick = onHeading) {
                Icon(imageVector = Icons.Default.Title, contentDescription = "Başlık")
            }
            IconButton(onClick = onBulletList) {
                Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Madde listesi"
                )
            }
            IconButton(onClick = onNumberedList) {
                Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = "Numaralı liste"
                )
            }
        }
    }
}
