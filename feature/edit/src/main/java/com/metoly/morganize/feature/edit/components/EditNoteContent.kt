package com.metoly.morganize.feature.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteContent
import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.grid.ChecklistActionType
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
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    onEmptyGridAddClicked: () -> Unit = {},
    onActivePageChanged: (Int) -> Unit = {}
) {
    NoteContent(
        title = uiState.title,
        onTitleChange = { onEvent(EditEvent.TitleChanged(it)) },
        titleHint = stringResource(R.string.feature_edit_title_hint),
        pages = uiState.pages,
        selectedItemId = uiState.selectedItemId,
        onItemSelected = { onEvent(EditEvent.ItemSelected(it)) },
        onItemMoved = { pageId, itemId, newX, newY ->
            onEvent(EditEvent.ItemMoved(pageId, itemId, newX, newY))
        },
        onItemResized = { pageId, itemId, newWidth, newHeight, newX, newY ->
            onEvent(EditEvent.ItemResized(pageId, itemId, newWidth, newHeight, newX, newY))
        },
        onItemTextChanged = { pageId, itemId, text ->
            onEvent(EditEvent.TextGridItemTextChanged(pageId, itemId, text))
        },
        onItemRichSpansChanged = { pageId, itemId, spans ->
            onEvent(EditEvent.TextGridItemRichSpansChanged(pageId, itemId, spans))
        },
        onItemTypographyChanged = { pageId, itemId, fontSize, textAlign, lineHeight ->
            onEvent(EditEvent.TextGridItemTypographyChanged(pageId, itemId, fontSize, textAlign, lineHeight))
        },
        onItemDeleted = { pageId, itemId ->
            onEvent(EditEvent.ItemDeleted(pageId, itemId))
        },
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
        // ── Drawing layer ────────────────────────────────────────────────
        isDrawingMode = uiState.isDrawingMode,
        isEraserMode = uiState.isEraserMode,
        penColorArgb = uiState.drawingPenColorArgb,
        strokeWidthFraction = uiState.drawingStrokeWidthFraction,
        eraserWidthFraction = uiState.drawingEraserWidthFraction,
        onStrokeAdded = { pageId, stroke ->
            onEvent(EditEvent.DrawingStrokeAdded(pageId, stroke))
        },
        onStrokesUpdated = { pageId, strokes ->
            onEvent(EditEvent.DrawingStrokesUpdated(pageId, strokes))
        },
        onChecklistTitleChanged = { pageId, itemId, title ->
            onEvent(EditEvent.ChecklistAction(pageId, itemId, ChecklistActionType.TitleChanged(title)))
        },
        onCheckboxToggled = { pageId, itemId, entryId ->
            onEvent(EditEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryToggled(entryId)))
        },
        onCheckboxTextChanged = { pageId, itemId, entryId, text ->
            onEvent(EditEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryTextChanged(entryId, text)))
        },
        onCheckboxAdded = { pageId, itemId ->
            onEvent(EditEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryAdded))
        },
        onCheckboxDeleted = { pageId, itemId, entryId ->
            onEvent(EditEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryDeleted(entryId)))
        },
        onEmptyGridAddClicked = onEmptyGridAddClicked,
        onActivePageChanged = onActivePageChanged,
        targetScrollPageIndex = uiState.targetScrollPageIndex,
        onScrollTargetHandled = { onEvent(EditEvent.ScrollTargetHandled) },
        modifier = modifier
    )
}