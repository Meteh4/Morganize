package com.metoly.components

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared bottom sheet for adding grid items (Text, Image, Checklist).
 * Replaces duplicated bottom sheet code in both CreateScreen and EditScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    onDismiss: () -> Unit,
    onAddText: () -> Unit,
    onAddImage: () -> Unit,
    onAddChecklist: () -> Unit,
    imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add Item",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val bottomSheetButtonStyle = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)

            Row(
                modifier = bottomSheetButtonStyle
                    .clickable {
                        onAddText()
                        onDismiss()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TextFields, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("Text", style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = bottomSheetButtonStyle
                    .clickable {
                        onAddImage()
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        onDismiss()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("Image", style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = bottomSheetButtonStyle
                    .clickable {
                        onAddChecklist()
                        onDismiss()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Checklist, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("Checklist", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
