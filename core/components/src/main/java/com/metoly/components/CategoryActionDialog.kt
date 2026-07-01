package com.metoly.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.metoly.morganize.core.model.Category

@Composable
fun CategoryActionDialog(
    category: Category,
    onDismiss: () -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Manage Category")
        },
        text = {
            Text(text = "What would you like to do with '${category.name}'?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEdit(category)
                    onDismiss()
                }
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        onDelete(category)
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
