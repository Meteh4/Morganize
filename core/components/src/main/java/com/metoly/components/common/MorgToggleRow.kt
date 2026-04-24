package com.metoly.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * A labelled switch row with title, subtitle, and a toggle.
 *
 * Extracted from the biometric toggle in [SetCredentialsBottomSheet].
 *
 * @param title Primary label.
 * @param subtitle Optional description text.
 * @param checked Whether the switch is on.
 * @param onCheckedChange Callback when the switch is toggled.
 */
@Composable
fun MorgToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(MorgShapes.toggleRow)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = MorgColors.RowContainerAlpha + 0.1f
                )
            )
            .padding(
                horizontal = MorgDimens.toggleRowPaddingH,
                vertical = MorgDimens.toggleRowPaddingV
            )
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
