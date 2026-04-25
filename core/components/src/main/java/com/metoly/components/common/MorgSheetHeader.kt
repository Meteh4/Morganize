package com.metoly.components.common

import androidx.compose.ui.graphics.painter.Painter


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Icon-box + title + subtitle header used at the top of every bottom sheet.
 *
 * Extracted from the security bottom sheets' common header pattern.
 *
 * @param icon Icon drawn inside the 44dp rounded container.
 * @param iconContainerColor Background of the icon container.
 *                           Defaults to `primaryContainer`.
 * @param iconTint Tint applied to the icon.
 *                 Defaults to `onPrimaryContainer`.
 * @param title Bold title text.
 * @param subtitle Smaller descriptive text below the title.
 */
@Composable
fun MorgSheetHeader(
    icon: Painter,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = MorgDimens.spacingXl)
    ) {
        Box(
            modifier = Modifier
                .size(MorgDimens.iconContainerSize)
                .clip(MorgShapes.iconContainer)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(MorgDimens.iconSize)
            )
        }
        Spacer(Modifier.width(MorgDimens.spacingMd + MorgDimens.spacingXxs))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
