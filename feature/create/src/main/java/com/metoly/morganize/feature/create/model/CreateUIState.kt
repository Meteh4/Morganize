// CreateUiState.kt
package com.metoly.morganize.feature.create.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.grid.DrawingStroke
import com.metoly.morganize.core.model.grid.NotePage
import java.util.UUID

data class CreateUiState(
    val backgroundColor: Int? = null,
    val categories: List<Category> = emptyList(),
    val categoryId: Long? = null,
    val drawingEraserWidthFraction: Float = 0.04f,
    val drawingPenColorArgb: Long = 0xFF000000L,
    val drawingStrokeWidthFraction: Float = 0.008f,
    val drawingUndoStack: Map<String, List<List<DrawingStroke>>> = emptyMap(),
    val editingRichState: RichTextEditorState? = null,
    val editingTextItemId: String? = null,
    val isDrawingMode: Boolean = false,
    val isDone: Boolean = false,
    val isEraserMode: Boolean = false,
    val pages: List<NotePage> = listOf(NotePage(id = UUID.randomUUID().toString())),
    val selectedItemId: String? = null,
    val title: String = "",
    val userMessage: String? = null,
    val targetScrollPageIndex: Int? = null,
)