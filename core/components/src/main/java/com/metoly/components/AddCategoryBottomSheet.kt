package com.metoly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val CategoryColors = listOf(
    0xFFE57373.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt(), 0xFF9575CD.toInt(), 
    0xFF7986CB.toInt(), 0xFF64B5F6.toInt(), 0xFF4FC3F7.toInt(), 0xFF4DD0E1.toInt(), 
    0xFF4DB6AC.toInt(), 0xFF81C784.toInt(), 0xFFAED581.toInt(), 0xFFFF8A65.toInt(), 
    0xFFA1887F.toInt(), 0xFF90A4AE.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, colorArgb: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var selectedColorArgb by remember { mutableIntStateOf(CategoryColors.first()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "New Category",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Color",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(CategoryColors) { colorArgb ->
                    val isSelected = selectedColorArgb == colorArgb
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorArgb))
                            .clickable { selectedColorArgb = colorArgb }
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), selectedColorArgb)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = name.isNotBlank()
                ) {
                    Text("Create")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
