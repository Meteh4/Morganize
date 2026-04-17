package com.metoly.components.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.DrawingStroke
import com.metoly.morganize.core.model.grid.TextAlignment

sealed interface NoteEditorEvent {
    // ── Grid Item ────────────────────────────────────────────────────────
    data class ItemMoved(val pageId: String, val itemId: String, val newX: Int, val newY: Int) : NoteEditorEvent
    data class ItemResized(val pageId: String, val itemId: String, val newWidth: Int, val newHeight: Int, val newX: Int, val newY: Int) : NoteEditorEvent
    data class ItemDeleted(val pageId: String, val itemId: String) : NoteEditorEvent
    data class ItemSelected(val itemId: String?) : NoteEditorEvent

    // ── Text ─────────────────────────────────────────────────────────────
    data class TextGridItemAdded(val text: String, val targetPageIndex: Int, val width: Int = 4, val height: Int = 4) : NoteEditorEvent
    data class TextGridItemTextChanged(val pageId: String, val itemId: String, val text: String) : NoteEditorEvent
    data class TextGridItemRichSpansChanged(val pageId: String, val itemId: String, val richSpans: List<RichSpan>) : NoteEditorEvent
    data class TextGridItemTypographyChanged(val pageId: String, val itemId: String, val fontSize: Float, val textAlign: TextAlignment, val lineHeight: Float) : NoteEditorEvent

    // ── Image ────────────────────────────────────────────────────────────
    data class ImageGridItemAdded(val path: String, val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : NoteEditorEvent

    // ── Checklist ────────────────────────────────────────────────────────
    data class ChecklistGridItemAdded(val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : NoteEditorEvent
    data class ChecklistAction(
        val pageId: String,
        val itemId: String,
        val action: ChecklistActionType
    ) : NoteEditorEvent

    // ── Drawing ──────────────────────────────────────────────────────────
    data object DrawingModeToggled : NoteEditorEvent
    data class DrawingColorChanged(val colorArgb: Long) : NoteEditorEvent
    data class DrawingStrokeWidthChanged(val widthFraction: Float) : NoteEditorEvent
    data class DrawingEraserWidthChanged(val widthFraction: Float) : NoteEditorEvent
    data object DrawingEraserToggled : NoteEditorEvent
    data class DrawingStrokeAdded(val pageId: String, val stroke: DrawingStroke) : NoteEditorEvent
    data class DrawingStrokeReverted(val pageId: String) : NoteEditorEvent
    data class DrawingStrokesUpdated(val pageId: String, val strokes: List<DrawingStroke>) : NoteEditorEvent

    // ── Page ─────────────────────────────────────────────────────────────
    data object AddPage : NoteEditorEvent

    // ── Rich Text Editing ────────────────────────────────────────────────
    data class EditingTextItemChanged(val itemId: String?) : NoteEditorEvent
    data class RichStateUpdated(val state: RichTextEditorState) : NoteEditorEvent

    // ── Meta ─────────────────────────────────────────────────────────────
    data class TitleChanged(val value: String) : NoteEditorEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : NoteEditorEvent
    data class CategorySelected(val categoryId: Long?) : NoteEditorEvent
    data class CreateCategory(val name: String, val colorArgb: Int) : NoteEditorEvent
}
