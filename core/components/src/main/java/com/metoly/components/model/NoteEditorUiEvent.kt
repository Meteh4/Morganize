package com.metoly.components.model

sealed interface NoteEditorUiEvent {
    data object SaveSuccess : NoteEditorUiEvent
    data class ShowSnackbar(val message: String) : NoteEditorUiEvent
    data class ScrollToPage(val pageIndex: Int) : NoteEditorUiEvent
}
