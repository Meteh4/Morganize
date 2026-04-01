package com.metoly.morganize.feature.edit.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.grid.DrawingStroke

sealed interface EditEvent {
    data class TitleChanged(val value: String) : EditEvent
    data object AddPage : EditEvent
    data class ItemSelected(val itemId: String?) : EditEvent
    data class ItemMoved(val pageId: String, val itemId: String, val newX: Int, val newY: Int) : EditEvent
    data class ItemResized(val pageId: String, val itemId: String, val newWidth: Int, val newHeight: Int, val newX: Int, val newY: Int) : EditEvent
    data class TextGridItemTextChanged(val pageId: String, val itemId: String, val text: String) : EditEvent
    data class TextGridItemRichSpansChanged(val pageId: String, val itemId: String, val richSpansJson: String) : EditEvent
    data class TextGridItemTypographyChanged(val pageId: String, val itemId: String, val fontSize: Float, val textAlign: String, val lineHeight: Float) : EditEvent
    data class TextGridItemAdded(val text: String) : EditEvent
    data class ImageGridItemAdded(val path: String) : EditEvent
    data class ItemDeleted(val pageId: String, val itemId: String) : EditEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : EditEvent
    data class CategorySelected(val categoryId: Long?) : EditEvent

    data class EditingTextItemChanged(val itemId: String?) : EditEvent
    data class RichStateUpdated(val state: RichTextEditorState) : EditEvent

    // ── Drawing events ──────────────────────────────────────────────────────

    /** Activates or deactivates the freehand drawing mode. */
    data object DrawingModeToggled : EditEvent

    /** Changes the active pen color (ARGB Long). */
    data class DrawingColorChanged(val colorArgb: Long) : EditEvent

    /** Changes the pen stroke width (canvas fraction). */
    data class DrawingStrokeWidthChanged(val widthFraction: Float) : EditEvent

    /** Changes the eraser circle radius (canvas fraction). */
    data class DrawingEraserWidthChanged(val widthFraction: Float) : EditEvent

    /** Toggles between pen and eraser tool. */
    data object DrawingEraserToggled : EditEvent

    /**
     * Appends a completed stroke to the specified page's drawing layer.
     * Called each time the user lifts their finger.
     */
    data class DrawingStrokeAdded(val pageId: String, val stroke: DrawingStroke) : EditEvent

    /**
     * Removes the last stroke from the specified page (Undo / Revert).
     * Operates on the currently active page. Pass null to use the first/only page.
     */
    data class DrawingStrokeReverted(val pageId: String) : EditEvent

    /**
     * Replaces the full stroke list on a page — used by the eraser which can
     * split/remove any subset of strokes.
     */
    data class DrawingStrokesUpdated(val pageId: String, val strokes: List<DrawingStroke>) : EditEvent

    // ── Lifecycle events ────────────────────────────────────────────────────

    data object Save : EditEvent
    data object DeleteRequested : EditEvent
    data object DeleteConfirmed : EditEvent
    data object DeleteDismissed : EditEvent
    data object NavigationHandled : EditEvent
    data object SnackbarDismissed : EditEvent
}