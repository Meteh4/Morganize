package com.metoly.morganize.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
        lightColorScheme(
                primary = Purple40,
                onPrimary = Gray99,
                primaryContainer = Purple90,
                onPrimaryContainer = Purple10,
                secondary = Teal40,
                onSecondary = Gray99,
                secondaryContainer = Teal90,
                onSecondaryContainer = Teal20,
                tertiary = Pink40,
                onTertiary = Gray99,
                tertiaryContainer = Pink90,
                onTertiaryContainer = Purple10,
                error = Red40,
                errorContainer = Red90,
                background = Gray99,
                onBackground = Gray10,
                surface = Gray99,
                onSurface = Gray10,
                surfaceVariant = Gray95,
                onSurfaceVariant = Gray20,
                outline = Gray20
        )

private val DarkColorScheme =
        darkColorScheme(
                primary = Purple80,
                onPrimary = Purple20,
                primaryContainer = Purple30,
                onPrimaryContainer = Purple90,
                secondary = Teal80,
                onSecondary = Teal20,
                secondaryContainer = Teal30,
                onSecondaryContainer = Teal90,
                tertiary = Pink80,
                onTertiary = Pink40,
                tertiaryContainer = Purple10,
                onTertiaryContainer = Pink90,
                error = Red80,
                errorContainer = Red40,
                background = Gray10,
                onBackground = Gray90,
                surface = Gray10,
                onSurface = Gray90,
                surfaceVariant = Gray20,
                onSurfaceVariant = Gray90,
                outline = Gray90
        )

/**
 * Top-level app theme wrapping [MaterialTheme]. Supports dynamic color on Android 12+ and falls
 * back to the custom palette on older devices.
 */
@Composable
fun MorganizeTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
