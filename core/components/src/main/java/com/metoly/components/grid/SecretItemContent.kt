package com.metoly.components.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * A locked representation for a secret grid item.
 * Obscures the content with a secure gradient overlay and lock icon.
 *
 * @param isBiometricDisabled Whether biometric unlocking is disabled (determines instructions text).
 * @param modifier Compose modifier.
 */
@Composable
fun SecretItemContent(
    isBiometricDisabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MorgShapes.gridItem)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(MorgDimens.spacingLg)
        ) {
            Box(
                modifier = Modifier
                    .size(MorgDimens.iconContainerSize)
                    .clip(MorgShapes.iconContainer)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secret Item",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(MorgDimens.iconSize)
                )
            }
            Spacer(modifier = Modifier.height(MorgDimens.spacingSm))
            Text(
                text = if (isBiometricDisabled) "Locked" else "Tap to Unlock",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
            )
        }
    }
}
