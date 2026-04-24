package com.metoly.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Small section header for grouping content.
 *
 * Used for labels like "Page 1", "Choose Color", etc.
 */
@Composable
fun MorgSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            horizontal = MorgDimens.spacingLg,
            vertical = MorgDimens.spacingSm
        )
    )
}
