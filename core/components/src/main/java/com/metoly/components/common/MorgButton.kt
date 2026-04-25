package com.metoly.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Full-width primary action button (52dp height, 12dp corners).
 */
@Composable
fun MorgPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MorgShapes.button,
        modifier = modifier
            .fillMaxWidth()
            .height(MorgDimens.buttonHeight)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Full-width destructive (error-coloured) button for delete / dangerous actions.
 */
@Composable
fun MorgDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MorgShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(MorgDimens.buttonHeight)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Full-width outlined button — secondary actions.
 */
@Composable
fun MorgOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MorgShapes.button,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

/**
 * Full-width subtle text button — cancel / dismiss actions.
 */
@Composable
fun MorgTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
