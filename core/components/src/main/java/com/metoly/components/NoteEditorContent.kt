package com.metoly.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorState
import com.metoly.morganize.core.model.grid.ChecklistActionType

/**
 * Shared NoteContent wrapper that bridges [NoteEditorState] and [NoteEditorEvent]
 * to the underlying [NoteContent] composable. Replaces both CreateNoteContent and EditNoteContent.
 */
@Composable
fun NoteEditorContent(
    modifier: Modifier = Modifier,
    state: NoteEditorState,
    onEvent: (NoteEditorEvent) -> Unit,
    titleHint: String,
    activeEditingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    onEmptyGridAddClicked: () -> Unit = {},
    onActivePageChanged: (Int) -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState()
) {
    NoteContent(
        title = state.title,
        onTitleChange = { onEvent(NoteEditorEvent.TitleChanged(it)) },
        titleHint = titleHint,
        pages = state.pages,
        selectedItemId = state.selectedItemId,
        onItemSelected = { onEvent(NoteEditorEvent.ItemSelected(it)) },
        onItemMoved = { pageId, itemId, newX, newY ->
            onEvent(NoteEditorEvent.ItemMoved(pageId, itemId, newX, newY))
        },
        onItemResized = { pageId, itemId, newWidth, newHeight, newX, newY ->
            onEvent(NoteEditorEvent.ItemResized(pageId, itemId, newWidth, newHeight, newX, newY))
        },
        onItemTextChanged = { pageId, itemId, text ->
            onEvent(NoteEditorEvent.TextGridItemTextChanged(pageId, itemId, text))
        },
        onItemRichSpansChanged = { pageId, itemId, spans ->
            onEvent(NoteEditorEvent.TextGridItemRichSpansChanged(pageId, itemId, spans))
        },
        onItemTypographyChanged = { pageId, itemId, fontSize, textAlign, lineHeight ->
            onEvent(NoteEditorEvent.TextGridItemTypographyChanged(pageId, itemId, fontSize, textAlign, lineHeight))
        },
        onItemDeleted = { pageId, itemId ->
            onEvent(NoteEditorEvent.ItemDeleted(pageId, itemId))
        },
        onEditingTextItemChanged = { itemId, richState ->
            onEvent(NoteEditorEvent.EditingTextItemChanged(itemId))
            if (richState != null) {
                onEvent(NoteEditorEvent.RichStateUpdated(richState))
            }
        },
        editingTextItemId = activeEditingTextItemId,
        activeRichState = activeRichState,
        onActiveRichStateChange = onActiveRichStateChange,
        categories = state.categories,
        selectedCategoryId = state.categoryId,
        onCategorySelected = { onEvent(NoteEditorEvent.CategorySelected(it)) },
        onAddPage = { onEvent(NoteEditorEvent.AddPage) },
        // ── Drawing layer ────────────────────────────────────────────────
        isDrawingMode = state.isDrawingMode,
        isEraserMode = state.isEraserMode,
        penColorArgb = state.drawingPenColorArgb,
        strokeWidthFraction = state.drawingStrokeWidthFraction,
        eraserWidthFraction = state.drawingEraserWidthFraction,
        onStrokeAdded = { pageId, stroke ->
            onEvent(NoteEditorEvent.DrawingStrokeAdded(pageId, stroke))
        },
        onStrokesUpdated = { pageId, strokes ->
            onEvent(NoteEditorEvent.DrawingStrokesUpdated(pageId, strokes))
        },
        onChecklistTitleChanged = { pageId, itemId, title ->
            onEvent(NoteEditorEvent.ChecklistAction(pageId, itemId, ChecklistActionType.TitleChanged(title)))
        },
        onCheckboxToggled = { pageId, itemId, entryId ->
            onEvent(NoteEditorEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryToggled(entryId)))
        },
        onCheckboxTextChanged = { pageId, itemId, entryId, text ->
            onEvent(NoteEditorEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryTextChanged(entryId, text)))
        },
        onCheckboxAdded = { pageId, itemId ->
            onEvent(NoteEditorEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryAdded))
        },
        onCheckboxDeleted = { pageId, itemId, entryId ->
            onEvent(NoteEditorEvent.ChecklistAction(pageId, itemId, ChecklistActionType.EntryDeleted(entryId)))
        },
        onEmptyGridAddClicked = onEmptyGridAddClicked,
        onActivePageChanged = onActivePageChanged,
        lazyListState = lazyListState,
        modifier = modifier
    )
}
