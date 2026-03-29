package com.metoly.morganize.feature.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteContent
import com.metoly.components.RichTextEditorState
import com.metoly.morganize.feature.edit.R
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState

@Composable
internal fun EditNoteContent(
    modifier: Modifier = Modifier,
    uiState: EditUiState,
    onEvent: (EditEvent) -> Unit,
    activeEditingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {}
) {
    NoteContent(
        title = uiState.title,
        onTitleChange = { onEvent(EditEvent.TitleChanged(it)) },
        titleHint = stringResource(R.string.feature_edit_title_hint),
        pages = uiState.pages,
        selectedItemId = uiState.selectedItemId,
        onItemSelected = { onEvent(EditEvent.ItemSelected(it)) },
        onItemMoved = { pId, iId, nX, nY -> onEvent(EditEvent.ItemMoved(pId, iId, nX, nY)) },
        onItemResized = { pId, iId, nW, nH, nX, nY ->
            onEvent(
                EditEvent.ItemResized(
                    pId,
                    iId,
                    nW,
                    nH,
                    nX,
                    nY
                )
            )
        },
        onItemTextChanged = { pId, iId, text ->
            onEvent(
                EditEvent.TextGridItemTextChanged(
                    pId,
                    iId,
                    text
                )
            )
        },
        onItemRichSpansChanged = { pId, iId, spansJson ->
            onEvent(
                EditEvent.TextGridItemRichSpansChanged(
                    pId,
                    iId,
                    spansJson
                )
            )
        },
        onItemTypographyChanged = { pId, iId, fs, ta, lh ->
            onEvent(
                EditEvent.TextGridItemTypographyChanged(
                    pId,
                    iId,
                    fs,
                    ta,
                    lh
                )
            )
        },
        onItemDeleted = { pId, iId -> onEvent(EditEvent.ItemDeleted(pId, iId)) },
        onEditingTextItemChanged = { itemId, richState ->
            onEvent(EditEvent.EditingTextItemChanged(itemId))
            if (richState != null) {
                onEvent(EditEvent.RichStateUpdated(richState))
            }
        },
        editingTextItemId = activeEditingTextItemId,
        activeRichState = activeRichState,
        onActiveRichStateChange = onActiveRichStateChange,
        categories = uiState.categories,
        selectedCategoryId = uiState.categoryId,
        onCategorySelected = { onEvent(EditEvent.CategorySelected(it)) },
        onAddPage = { onEvent(EditEvent.AddPage) },
        modifier = modifier
    )
}