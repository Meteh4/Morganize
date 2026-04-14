package com.metoly.morganize.feature.edit.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.DrawingStroke

sealed interface EditEvent {
    data class BackgroundColorChanged(val colorArgb: Int?) : EditEvent
    data class CategorySelected(val categoryId: Long?) : EditEvent
    data class ChecklistAction(
        val pageId: String,
        val itemId: String,
        val action: ChecklistActionType
    ) : EditEvent
    data class ChecklistGridItemAdded(val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : EditEvent
    data object DeleteConfirmed : EditEvent
    data object DeleteDismissed : EditEvent
    data object DeleteRequested : EditEvent
    data class DrawingColorChanged(val colorArgb: Long) : EditEvent
    data class DrawingEraserWidthChanged(val widthFraction: Float) : EditEvent
    data object DrawingEraserToggled : EditEvent
    data object DrawingModeToggled : EditEvent
    data class DrawingStrokeAdded(val pageId: String, val stroke: DrawingStroke) : EditEvent
    data class DrawingStrokeReverted(val pageId: String) : EditEvent
    data class DrawingStrokesUpdated(val pageId: String, val strokes: List<DrawingStroke>) : EditEvent
    data class DrawingStrokeWidthChanged(val widthFraction: Float) : EditEvent
    data class EditingTextItemChanged(val itemId: String?) : EditEvent
    data class ImageGridItemAdded(val path: String, val targetPageIndex: Int, val width: Int = 6, val height: Int = 6) : EditEvent
    data class ItemDeleted(val pageId: String, val itemId: String) : EditEvent
    data class ItemMoved(val pageId: String, val itemId: String, val newX: Int, val newY: Int) : EditEvent
    data class ItemResized(val pageId: String, val itemId: String, val newWidth: Int, val newHeight: Int, val newX: Int, val newY: Int) : EditEvent
    data class ItemSelected(val itemId: String?) : EditEvent
    data object NavigationHandled : EditEvent
    data object AddPage : EditEvent
    data class RichStateUpdated(val state: RichTextEditorState) : EditEvent
    data object Save : EditEvent
    data object SnackbarDismissed : EditEvent
    data class TextGridItemAdded(val text: String, val targetPageIndex: Int, val width: Int = 4, val height: Int = 4) : EditEvent
    data class TextGridItemRichSpansChanged(val pageId: String, val itemId: String, val richSpansJson: String) : EditEvent
    data class TextGridItemTextChanged(val pageId: String, val itemId: String, val text: String) : EditEvent
    data class TextGridItemTypographyChanged(val pageId: String, val itemId: String, val fontSize: Float, val textAlign: String, val lineHeight: Float) : EditEvent
    data class TitleChanged(val value: String) : EditEvent
    data object ScrollTargetHandled : EditEvent
}