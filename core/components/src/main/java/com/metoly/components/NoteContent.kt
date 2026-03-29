// NoteContent.kt
package com.metoly.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.metoly.components.grid.GridCanvas
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.grid.NotePage

private val TransparentFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    titleHint: String,
    pages: List<NotePage>,
    selectedItemId: String?,
    onItemSelected: (String?) -> Unit,
    onItemMoved: (pageId: String, itemId: String, newX: Int, newY: Int) -> Unit,
    onItemResized: (pageId: String, itemId: String, newWidth: Int, newHeight: Int, newX: Int, newY: Int) -> Unit,
    onItemTextChanged: (pageId: String, itemId: String, text: String) -> Unit,
    onItemRichSpansChanged: (pageId: String, itemId: String, richSpansJson: String) -> Unit,
    onItemTypographyChanged: (pageId: String, itemId: String, fontSize: Float, textAlign: String, lineHeight: Float) -> Unit,
    onItemDeleted: (pageId: String, itemId: String) -> Unit,
    onEditingTextItemChanged: (itemId: String?, richState: RichTextEditorState?) -> Unit,
    editingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddPage: () -> Unit,
    isReadOnly: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item(key = "header") {
            Spacer(Modifier.height(16.dp))

            CategoryChipRow(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                placeholder = { Text(titleHint, style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TransparentFieldColors
            )

            Spacer(Modifier.height(16.dp))
        }

        itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Page ${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                GridCanvas(
                    page = page,
                    selectedItemId = selectedItemId,
                    onItemSelected = onItemSelected,
                    onItemMoved = { itemId, newX, newY -> onItemMoved(page.id, itemId, newX, newY) },
                    onItemResized = { itemId, newW, newH, newX, newY ->
                        onItemResized(page.id, itemId, newW, newH, newX, newY)
                    },
                    onItemTextChanged = { itemId, text -> onItemTextChanged(page.id, itemId, text) },
                    onItemRichSpansChanged = { itemId, spansJson ->
                        onItemRichSpansChanged(page.id, itemId, spansJson)
                    },
                    onItemTypographyChanged = { itemId, fs, ta, lh ->
                        onItemTypographyChanged(page.id, itemId, fs, ta, lh)
                    },
                    onItemDeleted = { itemId -> onItemDeleted(page.id, itemId) },
                    onEditingTextItemChanged = onEditingTextItemChanged,
                    editingTextItemId = editingTextItemId,
                    activeRichState = activeRichState,
                    onActiveRichStateChange = onActiveRichStateChange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    isReadOnly = isReadOnly
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        item(key = "add_page") {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(onClick = onAddPage, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Note Page")
                }
            }
        }
    }
}