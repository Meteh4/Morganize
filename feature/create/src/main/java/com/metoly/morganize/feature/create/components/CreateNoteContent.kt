package com.metoly.morganize.feature.create.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteContent
import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.feature.create.R
import com.metoly.morganize.feature.create.model.CreateEvent
import com.metoly.morganize.feature.create.model.CreateUiState

@Composable
internal fun CreateNoteContent(
    modifier: Modifier = Modifier,
    uiState: CreateUiState,
    onEvent: (CreateEvent) -> Unit,
    activeEditingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    onEmptyGridAddClicked: () -> Unit = {},
    onActivePageChanged: (Int) -> Unit = {}
) {
    NoteContent(
        title = uiState.title,
        onTitleChange = { onEvent(CreateEvent.TitleChanged(it)) },
        titleHint = stringResource(R.string.feature_create_title_hint),
        pages = uiState.pages,
        selectedItemId = uiState.selectedItemId,
        onItemSelected = { onEvent(CreateEvent.ItemSelected(it)) },
        onItemMoved = { pageId, itemId, newX, newY ->
            onEvent(CreateEvent.ItemMoved(pageId, itemId, newX, newY))
        },
        onItemResized = { pageId, itemId, newWidth, newHeight, newX, newY ->
            onEvent(CreateEvent.ItemResized(pageId, itemId, newWidth, newHeight, newX, newY))
        },
        onItemTextChanged = { pageId, itemId, text ->
            onEvent(CreateEvent.TextGridItemTextChanged(pageId, itemId, text))
        },
        onItemRichSpansChanged = { pageId, itemId, spans ->
            onEvent(CreateEvent.TextGridItemRichSpansChanged(pageId, itemId, spans))
        },
        onItemTypographyChanged = { pageId, itemId, fontSize, textAlign, lineHeight ->
            onEvent(CreateEvent.TextGridItemTypographyChanged(pageId, itemId, fontSize, textAlign, lineHeight))
        },
        onItemDeleted = { pageId, itemId ->
            onEvent(CreateEvent.ItemDeleted(pageId, itemId))
        },
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
        onChecklistTitleChanged = { pageId, itemId, title ->
            onEvent(CreateEvent.ChecklistAction(pageId, itemId, ChecklistActionType.TitleChanged(title)))
        },
        onCheckboxToggled = { pageId, itemId, entryId ->
            onEvent(CreateEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryToggled(entryId)))
        },
        onCheckboxTextChanged = { pageId, itemId, entryId, text ->
            onEvent(CreateEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryTextChanged(entryId, text)))
        },
        onCheckboxAdded = { pageId, itemId ->
            onEvent(CreateEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryAdded))
        },
        onCheckboxDeleted = { pageId, itemId, entryId ->
            onEvent(CreateEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryDeleted(entryId)))
        },
        onEmptyGridAddClicked = onEmptyGridAddClicked,
        onActivePageChanged = onActivePageChanged,
        targetScrollPageIndex = uiState.targetScrollPageIndex,
        onScrollTargetHandled = { onEvent(CreateEvent.ScrollTargetHandled) },
        modifier = modifier
    )
}