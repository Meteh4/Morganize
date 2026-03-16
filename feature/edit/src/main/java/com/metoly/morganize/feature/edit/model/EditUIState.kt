package com.metoly.morganize.feature.edit.model

import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.ResponseState

data class EditUiState(
        val title: String = "",
        val content: String = "",
        val backgroundColor: Int? = null,
        val categoryId: Long? = null,
        val categories: List<Category> = emptyList(),
        val imagePaths: List<String> = emptyList(),
        val drawingPath: String? = null,
        val isMarkdownEnabled: Boolean = false,
        val checklistItems: List<ChecklistItem> = emptyList(),
        val noteState: ResponseState<Unit> = ResponseState.Idle,
        val showDeleteDialog: Boolean = false,
        val isDone: Boolean = false,
        val userMessage: String? = null
)
