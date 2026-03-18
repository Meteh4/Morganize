package com.metoly.morganize.feature.create.model

import androidx.compose.ui.text.input.TextFieldValue

sealed interface CreateEvent {
    data class TitleChanged(val value: String) : CreateEvent
    data class RichTextChanged(val value: TextFieldValue) : CreateEvent
    data object ToggleBold : CreateEvent
    data object ToggleItalic : CreateEvent
    data object ToggleBulletList : CreateEvent
    data object ToggleNumberedList : CreateEvent
    data object ContinueList : CreateEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : CreateEvent
    data class CategorySelected(val categoryId: Long?) : CreateEvent
    data class ImageAdded(val path: String) : CreateEvent
    data class ImageRemoved(val path: String) : CreateEvent
    data class DrawingChanged(val path: String?) : CreateEvent
    data class ChecklistItemAdded(val text: String) : CreateEvent
    data class ChecklistItemToggled(val index: Int) : CreateEvent
    data class ChecklistItemRemoved(val index: Int) : CreateEvent
    data class ChecklistItemTextChanged(val index: Int, val text: String) : CreateEvent
    data object Save : CreateEvent
    data object NavigationHandled : CreateEvent
    data object SnackbarDismissed : CreateEvent
}
