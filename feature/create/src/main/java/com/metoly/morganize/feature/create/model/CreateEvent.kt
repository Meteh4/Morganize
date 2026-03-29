package com.metoly.morganize.feature.create.model

import com.metoly.components.RichTextEditorState

sealed interface CreateEvent {
    data class TitleChanged(val value: String) : CreateEvent
    data object AddPage : CreateEvent
    data class ItemSelected(val itemId: String?) : CreateEvent
    data class ItemMoved(val pageId: String, val itemId: String, val newX: Int, val newY: Int) : CreateEvent
    data class ItemResized(val pageId: String, val itemId: String, val newWidth: Int, val newHeight: Int, val newX: Int, val newY: Int) : CreateEvent
    data class TextGridItemTextChanged(val pageId: String, val itemId: String, val text: String) : CreateEvent
    data class TextGridItemRichSpansChanged(val pageId: String, val itemId: String, val richSpansJson: String) : CreateEvent
    data class TextGridItemTypographyChanged(val pageId: String, val itemId: String, val fontSize: Float, val textAlign: String, val lineHeight: Float) : CreateEvent
    data class TextGridItemAdded(val text: String) : CreateEvent
    data class ImageGridItemAdded(val path: String) : CreateEvent
    data class ItemDeleted(val pageId: String, val itemId: String) : CreateEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : CreateEvent
    data class CategorySelected(val categoryId: Long?) : CreateEvent
    // Rich text toolbar actions
    data class EditingTextItemChanged(val itemId: String?) : CreateEvent
    data class RichStateUpdated(val state: RichTextEditorState) : CreateEvent

    data object Save : CreateEvent
    data object NavigationHandled : CreateEvent
    data object SnackbarDismissed : CreateEvent
}