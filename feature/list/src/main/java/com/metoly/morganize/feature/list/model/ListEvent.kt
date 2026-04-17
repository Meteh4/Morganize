package com.metoly.morganize.feature.list.model

import com.metoly.morganize.core.model.Note

sealed interface ListEvent {
    data class DeleteNote(val note: Note) : ListEvent
    data class FilterByCategory(val categoryId: Long?) : ListEvent
    data class CreateCategory(val name: String, val colorArgb: Int) : ListEvent
    data object SnackbarDismissed : ListEvent
}
