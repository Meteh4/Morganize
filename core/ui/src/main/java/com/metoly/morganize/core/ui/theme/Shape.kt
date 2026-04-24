package com.metoly.morganize.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Pre-built shape tokens referencing [MorgDimens] corner values.
 *
 * Usage: `MorgShapes.card`, `MorgShapes.button`, etc.
 */
object MorgShapes {

    val sheet get() = RoundedCornerShape(
        topStart = MorgDimens.sheetPadding,
        topEnd = MorgDimens.sheetPadding,
    )

    val button get() = RoundedCornerShape(MorgDimens.buttonCorner)

    val card get() = RoundedCornerShape(MorgDimens.cardCorner)

    val field get() = RoundedCornerShape(MorgDimens.fieldCorner)

    val iconContainer get() = RoundedCornerShape(MorgDimens.iconContainerCorner)

    val iconContainerMd get() = RoundedCornerShape(MorgDimens.iconContainerMdCorner)

    val iconContainerSm get() = RoundedCornerShape(MorgDimens.iconContainerSmCorner)

    val optionRow get() = RoundedCornerShape(MorgDimens.optionRowCorner)

    val toggleRow get() = RoundedCornerShape(MorgDimens.toggleRowCorner)

    val banner get() = RoundedCornerShape(MorgDimens.bannerCorner)

    val gridItem get() = RoundedCornerShape(MorgDimens.gridItemCorner)

    val toolbarToggle get() = RoundedCornerShape(MorgDimens.toolbarToggleCorner)

    val dialog get() = RoundedCornerShape(MorgDimens.dialogCorner)
}
