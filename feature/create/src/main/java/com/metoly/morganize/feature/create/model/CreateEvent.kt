package com.metoly.morganize.feature.create.model

sealed interface CreateEvent {
    data class TitleChanged(val value: String) : CreateEvent
    data class ContentChanged(val value: String) : CreateEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : CreateEvent
    data class CategorySelected(val categoryId: Long?) : CreateEvent
    data class ImageAdded(val path: String) : CreateEvent
    data class ImageRemoved(val path: String) : CreateEvent
    data class DrawingChanged(val path: String?) : CreateEvent
    data object MarkdownToggled : CreateEvent
    data class ChecklistItemAdded(val text: String) : CreateEvent
    data class ChecklistItemToggled(val index: Int) : CreateEvent
    data class ChecklistItemRemoved(val index: Int) : CreateEvent
    data class ChecklistItemTextChanged(val index: Int, val text: String) : CreateEvent
    data object Save : CreateEvent
    data object NavigationHandled : CreateEvent
    data object SnackbarDismissed : CreateEvent
}
