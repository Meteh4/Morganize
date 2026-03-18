package com.metoly.morganize.feature.edit.model

import androidx.compose.ui.text.input.TextFieldValue

sealed interface EditEvent {
    data class TitleChanged(val value: String) : EditEvent
    data class RichTextChanged(val value: TextFieldValue) : EditEvent
    data object ToggleBold : EditEvent
    data object ToggleItalic : EditEvent
    data object ToggleBulletList : EditEvent
    data object ToggleNumberedList : EditEvent
    data object ContinueList : EditEvent
    data class BackgroundColorChanged(val colorArgb: Int?) : EditEvent
    data class CategorySelected(val categoryId: Long?) : EditEvent
    data class ImageAdded(val path: String) : EditEvent
    data class ImageRemoved(val path: String) : EditEvent
    data class DrawingChanged(val path: String?) : EditEvent
    data class ChecklistItemAdded(val text: String) : EditEvent
    data class ChecklistItemToggled(val index: Int) : EditEvent
    data class ChecklistItemTextChanged(val index: Int, val text: String) : EditEvent
    data class ChecklistItemRemoved(val index: Int) : EditEvent
    data object Save : EditEvent
    data object DeleteRequested : EditEvent
    data object DeleteConfirmed : EditEvent
    data object DeleteDismissed : EditEvent
    data object NavigationHandled : EditEvent
    data object SnackbarDismissed : EditEvent
}
