package com.metoly.components.common

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Centered empty-state placeholder with:
 * - Large circular icon container
 * - Title
 * - Subtitle
 * - Optional action slot
 *
 * Used by: EmptyListContent, EmptyDetailPane, locked note detail, etc.
 */
@Composable
fun MorgEmptyState(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(MorgDimens.emptyStateIconContainer)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(MorgDimens.emptyStateIconSize)
                )
            }

            Spacer(Modifier.height(MorgDimens.spacingXxl))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (subtitle != null) {
                Spacer(Modifier.height(MorgDimens.spacingSm))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (action != null) {
                Spacer(Modifier.height(MorgDimens.spacingLg))
                action()
            }
        }
    }
}
