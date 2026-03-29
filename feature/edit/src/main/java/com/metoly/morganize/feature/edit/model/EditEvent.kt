package com.metoly.morganize.feature.edit.model

import com.metoly.components.RichTextEditorState

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

    data object Save : EditEvent
    data object DeleteRequested : EditEvent
    data object DeleteConfirmed : EditEvent
    data object DeleteDismissed : EditEvent
    data object NavigationHandled : EditEvent
    data object SnackbarDismissed : EditEvent
}