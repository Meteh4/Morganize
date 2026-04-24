package com.metoly.morganize.core.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary palette – deep indigo/violet ─────────────────────────────────
val Purple10 = Color(0xFF1A0043)
val Purple20 = Color(0xFF31008A)
val Purple30 = Color(0xFF4A00CC)
val Purple40 = Color(0xFF6200EE) // Primary
val Purple80 = Color(0xFFD0BBFF)
val Purple90 = Color(0xFFEADDFF)

// ── Secondary palette – teal/cyan ────────────────────────────────────────
val Teal20 = Color(0xFF003739)
val Teal30 = Color(0xFF004F52)
val Teal40 = Color(0xFF006B70)
val Teal80 = Color(0xFF4DD9E0)
val Teal90 = Color(0xFFB2EBEE)

// ── Tertiary – warm pink ─────────────────────────────────────────────────
val Pink40 = Color(0xFF984061)
val Pink80 = Color(0xFFFFB1C6)
val Pink90 = Color(0xFFFFD9E2)

// ── Neutral ──────────────────────────────────────────────────────────────
val Gray10 = Color(0xFF1C1B1F)
val Gray20 = Color(0xFF313033)
val Gray90 = Color(0xFFE6E1E5)
val Gray95 = Color(0xFFF4EFF4)
val Gray99 = Color(0xFFFFFBFE)

// ── Error ────────────────────────────────────────────────────────────────
val Red40 = Color(0xFFB3261E)
val Red80 = Color(0xFFF2B8B5)
val Red90 = Color(0xFFFFDAD6)

// ─────────────────────────────────────────────────────────────────────────
// Semantic tokens – used by components instead of raw hex values
// ─────────────────────────────────────────────────────────────────────────

/**
 * Categorised semantic colours used across options rows, icon containers,
 * banners and accent indicators.
 *
 * Every UI component should pull tints from here so visuals stay in sync.
 */
object MorgColors {
    // ── Icon container accents ───────────────────────────────────────
    val Blue = Color(0xFF4FC3F7)
    val Green = Color(0xFF81C784)
    val Orange = Color(0xFFFF8A65)
    val Purple = Color(0xFFAB47BC)
    val Red = Color(0xFFEF5350)
    val Cyan = Color(0xFF26C6DA)
    val Pink = Color(0xFFEC407A)
    val Yellow = Color(0xFFFFEE58)
    val Teal = Color(0xFF4DB6AC)

    /** Alpha applied to icon container backgrounds (e.g. option-row icons). */
    const val IconContainerAlpha = 0.15f

    // ── Strength bar colours ─────────────────────────────────────────
    val StrengthTooShort = Color(0xFFEF5350)
    val StrengthWeak = Color(0xFFFF7043)
    val StrengthFair = Color(0xFFFFA726)
    val StrengthGood = Color(0xFF66BB6A)
    val StrengthStrong = Color(0xFF43A047)

    // ── Surface helpers (used when theme doesn't provide the tone) ───
    /** Light-mode soft background (replaces hardcoded #F5F5F7). */
    val SurfaceSoftLight = Color(0xFFF5F5F7)
    /** Dark-mode soft background. */
    val SurfaceSoftDark = Color(0xFF1C1C1E)

    /** Option-row / toggle-row container overlay alpha. */
    const val RowContainerAlpha = 0.4f

    /** Grid item background alpha. */
    const val GridItemBackgroundAlpha = 0.6f
}
