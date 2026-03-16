package com.metoly.morganize.feature.list.model

import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState

data class ListUiState(
        val notesState: ResponseState<List<Note>> = ResponseState.Loading,
        val categories: List<Category> = emptyList(),
        val selectedCategoryId: Long? = null,
        val userMessage: String? = null
)
