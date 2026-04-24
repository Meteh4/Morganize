package com.metoly.morganize.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralised spacing, sizing and elevation tokens for the entire app.
 *
 * Every component should reference these instead of raw dp literals
 * so the visual rhythm stays consistent everywhere.
 */
object MorgDimens {

    // ── Spacing scale ────────────────────────────────────────────────
    val spacingXxs: Dp = 2.dp
    val spacingXs: Dp = 4.dp
    val spacingSm: Dp = 8.dp
    val spacingMd: Dp = 12.dp
    val spacingLg: Dp = 16.dp
    val spacingXl: Dp = 20.dp
    val spacingXxl: Dp = 24.dp
    val spacingXxxl: Dp = 32.dp

    // ── Sheet / Screen padding ───────────────────────────────────────
    val sheetPadding: Dp = 24.dp
    val screenPaddingHorizontal: Dp = 16.dp

    // ── Icon containers ──────────────────────────────────────────────
    /** Large icon containers (sheet headers). */
    val iconContainerSize: Dp = 44.dp
    val iconContainerCorner: Dp = 12.dp
    val iconSize: Dp = 22.dp

    /** Medium icon containers (option rows). */
    val iconContainerMdSize: Dp = 40.dp
    val iconContainerMdCorner: Dp = 10.dp

    /** Small icon containers (inline badges etc.). */
    val iconContainerSmSize: Dp = 32.dp
    val iconContainerSmCorner: Dp = 8.dp

    // ── Buttons ──────────────────────────────────────────────────────
    val buttonHeight: Dp = 52.dp
    val buttonCorner: Dp = 12.dp

    // ── Cards ────────────────────────────────────────────────────────
    val cardCorner: Dp = 16.dp
    val cardElevation: Dp = 0.dp

    // ── Text fields ──────────────────────────────────────────────────
    val fieldCorner: Dp = 12.dp

    // ── Option rows ──────────────────────────────────────────────────
    val optionRowCorner: Dp = 14.dp
    val optionRowPaddingH: Dp = 16.dp
    val optionRowPaddingV: Dp = 14.dp

    // ── Toggle rows ──────────────────────────────────────────────────
    val toggleRowCorner: Dp = 12.dp
    val toggleRowPaddingH: Dp = 16.dp
    val toggleRowPaddingV: Dp = 12.dp

    // ── Info banners ─────────────────────────────────────────────────
    val bannerCorner: Dp = 10.dp
    val bannerPadding: Dp = 12.dp

    // ── Grid ─────────────────────────────────────────────────────────
    val gridItemCorner: Dp = 12.dp
    val gridItemPadding: Dp = 8.dp
    val gridItemInnerPadding: Dp = 8.dp
    val gridItemSelectedBorder: Dp = 2.dp

    // ── Toolbar ──────────────────────────────────────────────────────
    val toolbarIconSize: Dp = 20.dp
    val toolbarButtonSize: Dp = 40.dp
    val toolbarToggleCorner: Dp = 8.dp
    val toolbarDividerHeight: Dp = 24.dp

    // ── Empty states ─────────────────────────────────────────────────
    val emptyStateIconContainer: Dp = 88.dp
    val emptyStateIconSize: Dp = 40.dp

    // ── Swatch (color pickers) ───────────────────────────────────────
    val swatchSize: Dp = 36.dp
    val swatchSmallSize: Dp = 28.dp

    // ── Dialog ───────────────────────────────────────────────────────
    val dialogCorner: Dp = 28.dp
    val dialogPadding: Dp = 24.dp
    val dialogIconContainerSize: Dp = 48.dp
}
