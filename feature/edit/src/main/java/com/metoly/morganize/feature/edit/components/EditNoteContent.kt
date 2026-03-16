package com.metoly.morganize.feature.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.metoly.components.CategoryChipRow
import com.metoly.components.MarkdownToolbar
import com.metoly.morganize.feature.edit.R
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState

private val transparentFieldColors
    @Composable
    get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

@Composable
internal fun EditNoteContent(
    uiState: EditUiState,
    onEvent: (EditEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.height(16.dp))

        CategoryChipRow(
            categories = uiState.categories,
            selectedCategoryId = uiState.categoryId,
            onCategorySelected = { onEvent(EditEvent.CategorySelected(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.imagePaths.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.imagePaths) { path ->
                    Box {
                        AsyncImage(
                            model = path,
                            contentDescription = "Not görseli",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        IconButton(
                            onClick = { onEvent(EditEvent.ImageRemoved(path)) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Sil",
                                tint = Color.White,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (uiState.drawingPath != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AsyncImage(
                    model = uiState.drawingPath,
                    contentDescription = "Çizim",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { onEvent(EditEvent.DrawingChanged(null)) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Çizimi Sil",
                        tint = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { onEvent(EditEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.feature_edit_title_hint),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            textStyle = MaterialTheme.typography.headlineSmall,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = transparentFieldColors
        )

        if (uiState.isMarkdownEnabled) {
            MarkdownToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onBold = { onEvent(EditEvent.ContentChanged(uiState.content + "**text**")) },
                onItalic = { onEvent(EditEvent.ContentChanged(uiState.content + "*text*")) },
                onHeading = { onEvent(EditEvent.ContentChanged(uiState.content + "\n# text")) },
                onBulletList = { onEvent(EditEvent.ContentChanged(uiState.content + "\n- text")) },
                onNumberedList = { onEvent(EditEvent.ContentChanged(uiState.content + "\n1. text")) }
            )
        }

        OutlinedTextField(
            value = uiState.content,
            onValueChange = { onEvent(EditEvent.ContentChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = {
                Text(
                    text = stringResource(R.string.feature_edit_content_hint),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            colors = transparentFieldColors
        )

        if (uiState.checklistItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            uiState.checklistItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { onEvent(EditEvent.ChecklistItemToggled(index)) }
                    )
                    OutlinedTextField(
                        value = item.text,
                        onValueChange = {
                            onEvent(EditEvent.ChecklistItemTextChanged(index, it))
                        },
                        modifier = Modifier.weight(1f),
                        colors = transparentFieldColors,
                        singleLine = true
                    )
                    IconButton(onClick = { onEvent(EditEvent.ChecklistItemRemoved(index)) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove item"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}