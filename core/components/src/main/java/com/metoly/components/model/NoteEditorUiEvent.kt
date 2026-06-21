package com.metoly.components.model

import com.metoly.morganize.core.ui.UiText

sealed interface NoteEditorUiEvent {
    data object SaveSuccess : NoteEditorUiEvent
    data class ShowSnackbar(val message: UiText) : NoteEditorUiEvent
    data class ScrollToPage(val pageIndex: Int) : NoteEditorUiEvent
    
    // Security UI events
    data class ShowBiometricPrompt(
        val itemId: String?, 
        val keystoreAlias: String,
        val decryptionIv: String? = null
    ) : NoteEditorUiEvent
    data class UnlockFailed(val message: String, val attemptsUsed: Int, val maxAttempts: Int = 5) : NoteEditorUiEvent
}
