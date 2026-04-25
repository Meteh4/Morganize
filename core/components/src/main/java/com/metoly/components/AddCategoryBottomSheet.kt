package com.metoly.components
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.metoly.components.common.MorgBottomSheet
import com.metoly.components.common.MorgOutlinedTextField
import com.metoly.components.common.MorgPrimaryButton
import com.metoly.components.common.MorgSectionHeader
import com.metoly.components.common.MorgSheetHeader
import com.metoly.components.common.MorgTextButton
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Predefined list of standard category colors for the palette selection.
 */
val CategoryColors = listOf(
    0xFFE57373.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt(), 0xFF9575CD.toInt(),
    0xFF7986CB.toInt(), 0xFF64B5F6.toInt(), 0xFF4FC3F7.toInt(), 0xFF4DD0E1.toInt(),
    0xFF4DB6AC.toInt(), 0xFF81C784.toInt(), 0xFFAED581.toInt(), 0xFFFF8A65.toInt(),
    0xFFA1887F.toInt(), 0xFF90A4AE.toInt()
)

/**
 * Bottom sheet allowing users to create a new category.
 * Provides a text field for the category name and a horizontal scrolling color palette.
 *
 * @param onDismiss Callback when bottom sheet is dismissed or cancelled.
 * @param onSave Callback triggered with the validated category name and selected argb color.
 */
@Composable
fun AddCategoryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, colorArgb: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColorArgb by remember { mutableIntStateOf(CategoryColors.first()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    MorgBottomSheet(onDismiss = onDismiss) {
        MorgSheetHeader(
            icon = painterResource(id = com.metoly.morganize.core.ui.R.drawable.category),
            title = "New Category",
            subtitle = "Create a label to organise notes"
        )

        MorgOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Category Name",
            focusRequester = focusRequester
        )

        Spacer(Modifier.height(MorgDimens.spacingXxl))

        MorgSectionHeader(text = "Choose Color")

        Spacer(Modifier.height(MorgDimens.spacingSm))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MorgDimens.spacingMd),
            contentPadding = PaddingValues(vertical = MorgDimens.spacingSm)
        ) {
            items(CategoryColors) { colorArgb ->
                val isSelected = selectedColorArgb == colorArgb
                val displayColor = Color(colorArgb)
                Box(
                    modifier = Modifier
                        .size(MorgDimens.swatchSize)
                        .clip(CircleShape)
                        .background(displayColor)
                        .clickable { selectedColorArgb = colorArgb }
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.tick),
                            contentDescription = null,
                            tint = if (displayColor.luminance() > 0.5f)
                                Color.Black.copy(alpha = 0.6f)
                            else
                                Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(MorgDimens.spacingXxl))

        MorgPrimaryButton(
            text = "Create",
            onClick = {
                if (name.isNotBlank()) {
                    onSave(name.trim(), selectedColorArgb)
                    onDismiss()
                }
            },
            enabled = name.isNotBlank()
        )

        Spacer(Modifier.height(MorgDimens.spacingSm))

        MorgTextButton(
            text = "Cancel",
            onClick = onDismiss
        )

        Spacer(Modifier.height(MorgDimens.spacingLg))
    }
}
