package com.metoly.components.security
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.ui.res.painterResource

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.metoly.components.common.MorgBottomSheet
import com.metoly.components.common.MorgPasswordField
import com.metoly.components.common.MorgPrimaryButton
import com.metoly.components.common.MorgSheetHeader
import com.metoly.components.common.MorgTextButton
import com.metoly.components.common.MorgToggleRow
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * Bottom sheet to set up credentials (password and optionally biometric) for a secret item or note override.
 * Displays password strength heuristically and requires password confirmation.
 *
 * @param title Hand header title text.
 * @param isBiometricAvailable Whether device biometric hardware is enrolled and available to use.
 * @param onConfirm Callback supplying the validated password and biometric opt-in state.
 * @param onDismiss Callback to dismiss without saving.
 */
@Composable
fun SetCredentialsBottomSheet(
    title: String = "Set Password",
    isBiometricAvailable: Boolean,
    onConfirm: (password: String, useBiometric: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var useBiometric by remember { mutableStateOf(isBiometricAvailable) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val isPasswordValid = password.length >= 4
    val doPasswordsMatch = password == confirmPassword && password.isNotEmpty()
    val checkFailed = password.isNotEmpty() && confirmPassword.isNotEmpty() && !doPasswordsMatch


    val strength = when {
        password.isEmpty() -> 0f
        password.length < 4 -> 0.2f
        password.length < 6 -> 0.4f
        password.length < 8 -> 0.6f
        password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 1f
        else -> 0.8f
    }
    val animatedStrength by animateFloatAsState(
        strength,
        animationSpec = MorgAnimation.standard(),
        label = "strength"
    )
    val strengthColor by animateColorAsState(
        when {
            strength <= 0.2f -> MorgColors.StrengthTooShort
            strength <= 0.4f -> MorgColors.StrengthWeak
            strength <= 0.6f -> MorgColors.StrengthFair
            strength <= 0.8f -> MorgColors.StrengthGood
            else -> MorgColors.StrengthStrong
        },
        label = "strength_color"
    )
    val strengthLabel = when {
        password.isEmpty() -> ""
        strength <= 0.2f -> "Too short"
        strength <= 0.4f -> "Weak"
        strength <= 0.6f -> "Fair"
        strength <= 0.8f -> "Good"
        else -> "Strong"
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MorgBottomSheet(onDismiss = onDismiss) {
        MorgSheetHeader(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.lock_locked),
            title = title,
            subtitle = "Protect this item with a password"
        )

        MorgPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Minimum 4 characters",
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            focusRequester = focusRequester
        )

        if (password.isNotEmpty()) {
            Spacer(Modifier.height(MorgDimens.spacingSm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(MorgShapes.banner)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedStrength)
                            .height(4.dp)
                            .clip(MorgShapes.banner)
                            .background(strengthColor)
                    )
                }
                Spacer(Modifier.width(MorgDimens.spacingSm + MorgDimens.spacingXxs))
                Text(
                    text = strengthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = strengthColor
                )
            }
        }

        Spacer(Modifier.height(MorgDimens.spacingMd))

        MorgPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isError = checkFailed,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                if (isPasswordValid && doPasswordsMatch) onConfirm(password, useBiometric)
            }),
            supportingText = if (checkFailed) {
                { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        Spacer(Modifier.height(MorgDimens.spacingLg))

        if (isBiometricAvailable) {
            MorgToggleRow(
                title = "Biometric Unlock",
                subtitle = "Use fingerprint or face to unlock",
                checked = useBiometric,
                onCheckedChange = { useBiometric = it }
            )
            Spacer(Modifier.height(MorgDimens.spacingXl))
        }

        MorgPrimaryButton(
            text = "Confirm",
            onClick = { onConfirm(password, useBiometric) },
            enabled = isPasswordValid && doPasswordsMatch
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgTextButton(
            text = "Cancel",
            onClick = onDismiss
        )

        Spacer(Modifier.height(MorgDimens.spacingLg))
    }
}
