package com.metoly.components.security

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.metoly.components.common.MorgBottomSheet
import com.metoly.components.common.MorgErrorBanner
import com.metoly.components.common.MorgPasswordField
import com.metoly.components.common.MorgPrimaryButton
import com.metoly.components.common.MorgSheetHeader
import com.metoly.components.common.MorgTextButton
import com.metoly.components.common.MorgWarningBanner
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgDimens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Bottom sheet to unlock a specific secret item or the entire note.
 * Handles tracking of invalid attempts, triggering an error shake animation, and
 * conditionally disabling biometric unlock on overuse.
 *
 * @param title Header title.
 * @param errorCount Number of currently failed attempts.
 * @param maxAttempts Number of fails permitted before warnings escalate.
 * @param showBiometricButton If true, displays a shortcut button to trigger the biometric prompt.
 * @param onConfirm Callback supplying the submitted password attempt.
 * @param onBiometricRequested Callback when the user taps 'Use Biometric'.
 * @param onDismiss Callback to close the prompt.
 */
@Composable
fun UnlockBottomSheet(
    title: String = "Unlock",
    errorCount: Int = 0,
    maxAttempts: Int = 5,
    showBiometricButton: Boolean = false,
    onConfirm: (String) -> Unit,
    onBiometricRequested: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorCount) {
        if (errorCount > 0) {
            showError = true
            scope.launch {
                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = MorgAnimation.shake(),
                    initialVelocity = 3000f
                )
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MorgBottomSheet(onDismiss = onDismiss) {
        MorgSheetHeader(
            icon = Icons.Default.Lock,
            title = title,
            subtitle = "Enter password to access this item",
            iconContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
            iconTint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
        )

        if (errorCount >= maxAttempts) {
            MorgErrorBanner(
                text = "Biometric disabled due to too many failed attempts. Please use your password."
            )
            Spacer(Modifier.height(MorgDimens.spacingMd))
        } else if (showError && errorCount > 0) {
            MorgWarningBanner(
                text = "Incorrect password. Attempt $errorCount of $maxAttempts"
            )
            Spacer(Modifier.height(MorgDimens.spacingMd))
        }

        MorgPasswordField(
            value = password,
            onValueChange = {
                password = it
                showError = false
            },
            label = "Password",
            isError = showError && errorCount > 0,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                if (password.isNotBlank()) onConfirm(password)
            }),
            focusRequester = focusRequester,
            modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        )

        Spacer(Modifier.height(MorgDimens.spacingXl))

        MorgPrimaryButton(
            text = "Unlock",
            onClick = { onConfirm(password) },
            enabled = password.isNotBlank()
        )

        if (showBiometricButton) {
            Spacer(Modifier.height(MorgDimens.spacingSm))
            TextButton(
                onClick = onBiometricRequested,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(MorgDimens.spacingSm))
                Text("Use Biometric")
            }
        }

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgTextButton(
            text = "Cancel",
            onClick = onDismiss
        )

        Spacer(Modifier.height(MorgDimens.spacingLg))
    }
}
