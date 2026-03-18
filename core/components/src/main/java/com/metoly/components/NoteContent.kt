package com.metoly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.metoly.morganize.core.model.Category

data class ChecklistItemUi(val text: String, val isChecked: Boolean)

@Composable
fun noteTransparentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

/**
 * Unified note content area used by both Create and Edit screens.
 *
 * The title uses a plain [OutlinedTextField].
 * The body uses [RichTextEditor] with native AnnotatedString formatting.
 * The [RichTextToolbar] is shown inline above the content and always visible.
 */
@Composable
fun NoteContent(
    title: String,
    onTitleChange: (String) -> Unit,
    titleHint: String,
    richTextState: RichTextEditorState,
    onRichTextChange: (TextFieldValue) -> Unit,
    onEnterPressed: () -> Unit,
    contentHint: String,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    imagePaths: List<String>,
    onImageRemoved: (String) -> Unit,
    drawingPath: String?,
    onDrawingRemoved: () -> Unit,
    checklistItems: List<ChecklistItemUi>,
    onChecklistItemToggled: (Int) -> Unit,
    onChecklistItemTextChanged: (Int, String) -> Unit,
    onChecklistItemRemoved: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val fieldColors = noteTransparentFieldColors()

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))

        CategoryChipRow(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected
        )

        Spacer(Modifier.height(16.dp))

        if (imagePaths.isNotEmpty()) {
            NoteImageRow(imagePaths = imagePaths, onImageRemoved = onImageRemoved)
            Spacer(Modifier.height(16.dp))
        }

        if (drawingPath != null) {
            NoteDrawingPreview(drawingPath = drawingPath, onDrawingRemoved = onDrawingRemoved)
            Spacer(Modifier.height(16.dp))
        }

        // Title field
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(titleHint, style = MaterialTheme.typography.headlineSmall) },
            textStyle = MaterialTheme.typography.headlineSmall,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = fieldColors
        )

        Spacer(Modifier.height(4.dp))

        // Rich text content field
        RichTextEditor(
            state = richTextState,
            onStateChange = onRichTextChange,
            onEnterPressed = onEnterPressed,
            textStyle = MaterialTheme.typography.bodyLarge,
            hint = {
                Text(
                    text = contentHint,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 200.dp)
                .padding(horizontal = 16.dp)
        )

        if (checklistItems.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            NoteChecklistSection(
                items = checklistItems,
                onItemToggled = onChecklistItemToggled,
                onItemTextChanged = onChecklistItemTextChanged,
                onItemRemoved = onChecklistItemRemoved,
                fieldColors = fieldColors
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NoteImageRow(imagePaths: List<String>, onImageRemoved: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(imagePaths) { path ->
            Box {
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { onImageRemoved(path) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDrawingPreview(drawingPath: String, onDrawingRemoved: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AsyncImage(
            model = drawingPath,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        IconButton(
            onClick = onDrawingRemoved,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun NoteChecklistSection(
    items: List<ChecklistItemUi>,
    onItemToggled: (Int) -> Unit,
    onItemTextChanged: (Int, String) -> Unit,
    onItemRemoved: (Int) -> Unit,
    fieldColors: TextFieldColors
) {
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onItemToggled(index) }
            )
            OutlinedTextField(
                value = item.text,
                onValueChange = { onItemTextChanged(index, it) },
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                singleLine = true
            )
            IconButton(onClick = { onItemRemoved(index) }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
}