package com.metoly.components.security

import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.metoly.components.common.MorgBottomSheet
import com.metoly.components.common.MorgOptionRow
import com.metoly.components.common.MorgSheetHeader
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Bottom sheet prompt for choosing the underlying content type of a new secret item.
 *
 * @param onTextSelected Callback to create a secret text item.
 * @param onChecklistSelected Callback to create a secret checklist item.
 * @param onImageSelected Callback to prompt image selection for a secret image item.
 * @param onDismiss Callback to dismiss the builder sequence.
 */
@Composable
fun SecretItemTypePickerBottomSheet(
    onTextSelected: () -> Unit,
    onChecklistSelected: () -> Unit,
    onImageSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    MorgBottomSheet(onDismiss = onDismiss) {
        MorgSheetHeader(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.lock_locked),
            title = "Secret Item",
            subtitle = "Choose what to protect"
        )

        Spacer(Modifier.height(MorgDimens.spacingLg))

        MorgOptionRow(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.text_item),
            title = "Text",
            description = "Protected text content",
            iconTint = MorgColors.Blue,
            onClick = {
                onTextSelected()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgOptionRow(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.checklist),
            title = "Checklist",
            description = "Protected checklist with tasks",
            iconTint = MorgColors.Green,
            onClick = {
                onChecklistSelected()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgOptionRow(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.image_item),
            title = "Image",
            description = "Protected image content",
            iconTint = MorgColors.Orange,
            onClick = {
                onImageSelected()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingXxl))
    }
}
