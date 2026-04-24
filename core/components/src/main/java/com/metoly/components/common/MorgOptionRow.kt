package com.metoly.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Tappable option row used inside bottom sheets and pickers.
 *
 * Extracted from `SecretTypeOption` in [SecretItemTypePickerBottomSheet].
 *
 * Features:
 * - 40dp rounded icon container with tinted alpha background
 * - Title + description text
 * - 14dp-rounded outer container with surfaceVariant background
 *
 * @param icon Leading icon.
 * @param title Primary label.
 * @param description Secondary descriptive text.
 * @param iconTint Colour for both the icon and its container's tinted-alpha background.
 * @param onClick Callback when the row is tapped.
 */
@Composable
fun MorgOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MorgShapes.optionRow)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = MorgColors.RowContainerAlpha))
            .clickable(onClick = onClick)
            .padding(
                horizontal = MorgDimens.optionRowPaddingH,
                vertical = MorgDimens.optionRowPaddingV
            )
    ) {
        Box(
            modifier = Modifier
                .size(MorgDimens.iconContainerMdSize)
                .clip(MorgShapes.iconContainerMd)
                .background(iconTint.copy(alpha = MorgColors.IconContainerAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(MorgDimens.iconSize)
            )
        }
        Spacer(Modifier.width(MorgDimens.spacingMd + MorgDimens.spacingXxs))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
