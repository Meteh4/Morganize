package com.metoly.morganize.feature.create.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.grid.NotePage
import java.util.UUID

data class CreateUiState(
    val title: String = "",
    val pages: List<NotePage> = listOf(NotePage(id = UUID.randomUUID().toString())),
    val selectedItemId: String? = null,
    val backgroundColor: Int? = null,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isDone: Boolean = false,
    val userMessage: String? = null,
    // Rich text toolbar state
    val editingTextItemId: String? = null,
    val editingRichState: RichTextEditorState? = null,
)
