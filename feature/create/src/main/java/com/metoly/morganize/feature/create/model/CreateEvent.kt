package com.metoly.morganize.feature.create.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.DrawingStroke

sealed interface CreateEvent {
    data class BackgroundColorChanged(val colorArgb: Int?) : CreateEvent
    data class CategorySelected(val categoryId: Long?) : CreateEvent
    data class ChecklistAction(
        val pageId: String,
        val itemId: String,
        val action: ChecklistActionType
    ) : CreateEvent
    data class ChecklistGridItemAdded(val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : CreateEvent
    data class DrawingColorChanged(val colorArgb: Long) : CreateEvent
    data class DrawingEraserWidthChanged(val widthFraction: Float) : CreateEvent
    data object DrawingEraserToggled : CreateEvent
    data object DrawingModeToggled : CreateEvent
    data class DrawingStrokeAdded(val pageId: String, val stroke: DrawingStroke) : CreateEvent
    data class DrawingStrokeReverted(val pageId: String) : CreateEvent
    data class DrawingStrokesUpdated(val pageId: String, val strokes: List<DrawingStroke>) : CreateEvent
    data class DrawingStrokeWidthChanged(val widthFraction: Float) : CreateEvent
    data class EditingTextItemChanged(val itemId: String?) : CreateEvent
    data class ImageGridItemAdded(val path: String, val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : CreateEvent
    data class ItemDeleted(val pageId: String, val itemId: String) : CreateEvent
    data class ItemMoved(val pageId: String, val itemId: String, val newX: Int, val newY: Int) : CreateEvent
    data class ItemResized(val pageId: String, val itemId: String, val newWidth: Int, val newHeight: Int, val newX: Int, val newY: Int) : CreateEvent
    data class ItemSelected(val itemId: String?) : CreateEvent
    data object NavigationHandled : CreateEvent
    data object AddPage : CreateEvent
    data class RichStateUpdated(val state: RichTextEditorState) : CreateEvent
    data object Save : CreateEvent
    data object SnackbarDismissed : CreateEvent
    data class TextGridItemAdded(val text: String, val targetPageIndex: Int, val width: Int = 4, val height: Int = 4) : CreateEvent
    data class TextGridItemRichSpansChanged(val pageId: String, val itemId: String, val richSpans: List<RichSpan>) : CreateEvent
    data class TextGridItemTextChanged(val pageId: String, val itemId: String, val text: String) : CreateEvent
    data class TextGridItemTypographyChanged(val pageId: String, val itemId: String, val fontSize: Float, val textAlign: String, val lineHeight: Float) : CreateEvent
    data class TitleChanged(val value: String) : CreateEvent
    data object ScrollTargetHandled : CreateEvent
}