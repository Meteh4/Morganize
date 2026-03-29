package com.metoly.morganize.feature.edit.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.grid.NotePage
import java.util.UUID

data class EditUiState(
    val title: String = "",
    val pages: List<NotePage> = listOf(NotePage(id = UUID.randomUUID().toString())),
    val selectedItemId: String? = null,
    val backgroundColor: Int? = null,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val noteState: ResponseState<Unit> = ResponseState.Idle,
    val showDeleteDialog: Boolean = false,
    val isDone: Boolean = false,
    val userMessage: String? = null,
    // Rich text toolbar state – non-null when a text item is actively being edited
    val editingTextItemId: String? = null,
    val editingRichState: RichTextEditorState? = null,
)
