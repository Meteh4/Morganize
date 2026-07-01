package com.metoly.morganize.feature.list.model

import com.metoly.morganize.core.model.Note
import android.net.Uri

/**
 * Sort options for the note list.
 */
enum class NoteSortOrder {
    /** Most recently updated first (default). */
    UPDATED_DESC,
    /** Oldest updates first. */
    UPDATED_ASC,
    /** Alphabetical A→Z. */
    TITLE_ASC,
    /** Alphabetical Z→A. */
    TITLE_DESC,
    /** Newest created first. */
    CREATED_DESC
}

/**
 * View mode for the note list.
 */
enum class NoteViewMode {
    LIST,
    GRID
}

sealed interface ListEvent {
    data class DeleteNote(val note: Note) : ListEvent
    data class FilterByCategory(val categoryId: Long?) : ListEvent
    data class CreateCategory(val name: String, val colorArgb: Int) : ListEvent
    data object SnackbarDismissed : ListEvent

    // Faz 1 events
    data class SearchQueryChanged(val query: String) : ListEvent
    data class TogglePin(val noteId: Long, val isPinned: Boolean) : ListEvent
    data class SortOrderChanged(val order: NoteSortOrder) : ListEvent
    data object ToggleViewMode : ListEvent
    data class UndoDelete(val noteId: Long) : ListEvent
    data class DuplicateNote(val noteId: Long) : ListEvent
    data class ExportNotes(val uri: Uri) : ListEvent
    data class ImportNotes(val uri: Uri) : ListEvent
}
