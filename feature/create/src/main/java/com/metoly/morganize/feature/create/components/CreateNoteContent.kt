package com.metoly.morganize.feature.create.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteContent
import com.metoly.morganize.feature.create.R
import com.metoly.morganize.feature.create.model.CreateEvent
import com.metoly.morganize.feature.create.model.CreateUiState

@Composable
internal fun CreateNoteContent(
    modifier: Modifier = Modifier,
    uiState: CreateUiState,
    onEvent: (CreateEvent) -> Unit,
    activeEditingTextItemId: String? = null,
    activeRichState: com.metoly.components.RichTextEditorState? = null,
    onActiveRichStateChange: (com.metoly.components.RichTextEditorState) -> Unit = {}
) {
    NoteContent(
        title = uiState.title,
        onTitleChange = { onEvent(CreateEvent.TitleChanged(it)) },
        titleHint = stringResource(R.string.feature_create_title_hint),
        pages = uiState.pages,
        selectedItemId = uiState.selectedItemId,
        onItemSelected = { onEvent(CreateEvent.ItemSelected(it)) },
        onItemMoved = { pId, iId, nX, nY -> onEvent(CreateEvent.ItemMoved(pId, iId, nX, nY)) },
        onItemResized = { pId, iId, nW, nH, nX, nY -> onEvent(CreateEvent.ItemResized(pId, iId, nW, nH, nX, nY)) },
        onItemTextChanged = { pId, iId, text -> onEvent(CreateEvent.TextGridItemTextChanged(pId, iId, text)) },
        onItemRichSpansChanged = { pId, iId, spansJson -> onEvent(CreateEvent.TextGridItemRichSpansChanged(pId, iId, spansJson)) },
        onItemTypographyChanged = { pId, iId, fs, ta, lh -> onEvent(CreateEvent.TextGridItemTypographyChanged(pId, iId, fs, ta, lh)) },
        onItemDeleted = { pId, iId -> onEvent(CreateEvent.ItemDeleted(pId, iId)) },
        onEditingTextItemChanged = { itemId, richState ->
            onEvent(CreateEvent.EditingTextItemChanged(itemId))
            if (richState != null) {
                onEvent(CreateEvent.RichStateUpdated(richState))
            }
        },
        editingTextItemId = activeEditingTextItemId,
        activeRichState = activeRichState,
        onActiveRichStateChange = onActiveRichStateChange,
        categories = uiState.categories,
        selectedCategoryId = uiState.categoryId,
        onCategorySelected = { onEvent(CreateEvent.CategorySelected(it)) },
        onAddPage = { onEvent(CreateEvent.AddPage) },
        // ── Drawing layer ────────────────────────────────────────────────
        isDrawingMode = uiState.isDrawingMode,
        isEraserMode = uiState.isEraserMode,
        penColorArgb = uiState.drawingPenColorArgb,
        strokeWidthFraction = uiState.drawingStrokeWidthFraction,
        eraserWidthFraction = uiState.drawingEraserWidthFraction,
        onStrokeAdded = { pageId, stroke ->
            onEvent(CreateEvent.DrawingStrokeAdded(pageId, stroke))
        },
        onStrokesUpdated = { pageId, strokes ->
            onEvent(CreateEvent.DrawingStrokesUpdated(pageId, strokes))
        },
        // ── Checklist callbacks ──────────────────────────────────────────
        onChecklistTitleChanged = { pId, iId, title ->
            onEvent(CreateEvent.ChecklistTitleChanged(pId, iId, title))
        },
        onCheckboxToggled = { pId, iId, entryId ->
            onEvent(CreateEvent.CheckboxToggled(pId, iId, entryId))
        },
        onCheckboxTextChanged = { pId, iId, entryId, text ->
            onEvent(CreateEvent.CheckboxTextChanged(pId, iId, entryId, text))
        },
        onCheckboxAdded = { pId, iId ->
            onEvent(CreateEvent.CheckboxAdded(pId, iId))
        },
        onCheckboxDeleted = { pId, iId, entryId ->
            onEvent(CreateEvent.CheckboxDeleted(pId, iId, entryId))
        },
        modifier = modifier
    )
}