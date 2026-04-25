package com.metoly.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Error banner — extracted from UnlockBottomSheet error messages.
 */
@Composable
fun MorgErrorBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MorgShapes.banner)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(MorgDimens.bannerPadding)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Softer error banner with reduced alpha — for non-critical warnings.
 */
@Composable
fun MorgWarningBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MorgShapes.banner)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
            .padding(MorgDimens.bannerPadding)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Neutral informational banner.
 */
@Composable
fun MorgInfoBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MorgShapes.banner)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(MorgDimens.bannerPadding)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
