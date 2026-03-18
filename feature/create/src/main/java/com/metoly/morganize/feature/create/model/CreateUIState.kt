package com.metoly.morganize.feature.create.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ChecklistItem

data class CreateUiState(
        val title: String = "",
        val richTextState: RichTextEditorState = RichTextEditorState(),
        val backgroundColor: Int? = null,
        val categoryId: Long? = null,
        val categories: List<Category> = emptyList(),
        val imagePaths: List<String> = emptyList(),
        val drawingPath: String? = null,
        val checklistItems: List<ChecklistItem> = emptyList(),
        val isDone: Boolean = false,
        val userMessage: String? = null
)
