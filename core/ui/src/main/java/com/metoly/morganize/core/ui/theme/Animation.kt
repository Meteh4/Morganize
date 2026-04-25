package com.metoly.morganize.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Standardised animation specs so every transition in the app feels cohesive.
 */
object MorgAnimation {

    /** Fast, responsive spring – buttons, toggles, small state changes. */
    fun <T> snappy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    /** Default spring – most UI transitions. */
    fun <T> standard() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Gentle spring – page/layout transitions. */
    fun <T> gentle() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Bouncy spring – playful entry animations. */
    fun <T> bouncy() = spring<T>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMedium
    )

    /** Shake spring – error feedback. */
    fun <T> shake() = spring<T>(
        dampingRatio = 0.2f,
        stiffness = 3000f
    )
}
