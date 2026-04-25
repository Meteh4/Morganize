package com.metoly.components.common

import androidx.compose.ui.graphics.painter.Painter


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Custom dialog with icon header, title, body, and action buttons.
 *
 * Replaces stock [AlertDialog] for a consistent premium look.
 *
 * @param icon Header icon.
 * @param iconContainerColor Background of the 48dp icon circle.
 * @param iconTint Tint of the icon.
 * @param title Dialog title.
 * @param text Body text.
 * @param confirmButton Primary action composable.
 * @param dismissButton Secondary action composable.
 * @param onDismiss Called when the dialog is dismissed (back / outside tap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorgDialog(
    icon: Painter,
    title: String,
    text: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    iconContainerColor: Color = MaterialTheme.colorScheme.errorContainer,
    iconTint: Color = MaterialTheme.colorScheme.error,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .clip(MorgShapes.dialog)
                .background(MaterialTheme.colorScheme.surface)
                .padding(MorgDimens.dialogPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(MorgDimens.dialogIconContainerSize)
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

            Spacer(Modifier.height(MorgDimens.spacingLg))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(MorgDimens.spacingSm))

            // Body
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(MorgDimens.spacingXxl))

            // Actions
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    dismissButton()
                }
                Spacer(Modifier.width(MorgDimens.spacingSm))
                Box(modifier = Modifier.weight(1f)) {
                    confirmButton()
                }
            }
        }
    }
}
