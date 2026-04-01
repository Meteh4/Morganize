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
    // ── Drawing mode state ────────────────────────────────────────────────
    /** True when the freehand drawing overlay is active. */
    val isDrawingMode: Boolean = false,
    /** Currently selected pen color as ARGB Long. */
    val drawingPenColorArgb: Long = 0xFF000000L,
    /** Pen stroke width as a fraction of the canvas width (density-independent). */
    val drawingStrokeWidthFraction: Float = 0.008f,
    /** Eraser radius as a fraction of the canvas width. */
    val drawingEraserWidthFraction: Float = 0.04f,
    /** True when the eraser tool is currently active. */
    val isEraserMode: Boolean = false,
    /** History of drawing strings per page to support full drawing and eraser reversions. */
    val drawingUndoStack: Map<String, List<String>> = emptyMap(),
)
