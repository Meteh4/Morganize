package com.metoly.morganize.feature.list.model

import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState

data class ListUiState(
    val notesState: ResponseState<List<Note>> = ResponseState.Idle,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val userMessage: com.metoly.morganize.core.ui.UiText? = null,
    // Faz 1 additions
    val searchQuery: String = "",
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC,
    val viewMode: NoteViewMode = NoteViewMode.LIST,
    /** ID of the note that was just soft-deleted — available for undo. */
    val pendingDeleteNoteId: Long? = null
)
