package com.metoly.components

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.Composable
import com.metoly.components.common.MorgBottomSheet
import com.metoly.components.common.MorgOptionRow
import com.metoly.components.common.MorgSheetHeader
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Bottom sheet menu for adding grid items (Text, Image, Checklist, Secret Item) to empty grids.
 * Provides a highly visible set of options for scaffolding notes.
 *
 * @param onDismiss Callback when the bottom sheet is dismissed.
 * @param onAddText Callback to instantiate a new text grid item.
 * @param onAddImage Callback to open the OS visual media picker.
 * @param onAddChecklist Callback to instantiate a new checklist grid item.
 * @param onAddSecretItem Callback to initiate secret item scaffolding.
 * @param imagePickerLauncher The configured launcher for picking visual media.
 */
@Composable
fun AddItemBottomSheet(
    onDismiss: () -> Unit,
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    onAddChecklist: () -> Unit,
    onAddSecretItem: () -> Unit = {},
    imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>
) {
    MorgBottomSheet(onDismiss = onDismiss) {
        MorgSheetHeader(
            icon = Icons.Default.Add,
            title = "Add Item",
            subtitle = "Choose content type to add"
        )

        MorgOptionRow(
            icon = Icons.Default.TextFields,
            title = "Text",
            description = "Rich text with formatting",
            iconTint = MorgColors.Blue,
            onClick = {
                onAddText()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgOptionRow(
            icon = Icons.Default.Image,
            title = "Image",
            description = "Photo or picture from gallery",
            iconTint = MorgColors.Orange,
            onClick = {
                onAddImage()
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgOptionRow(
            icon = Icons.Default.Checklist,
            title = "Checklist",
            description = "Checkboxes with tasks",
            iconTint = MorgColors.Green,
            onClick = {
                onAddChecklist()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgOptionRow(
            icon = Icons.Default.Lock,
            title = "Secret Item",
            description = "Password-protected content",
            iconTint = MorgColors.Purple,
            onClick = {
                onAddSecretItem()
                onDismiss()
            }
        )

        Spacer(Modifier.height(MorgDimens.spacingXxl))
    }
}
