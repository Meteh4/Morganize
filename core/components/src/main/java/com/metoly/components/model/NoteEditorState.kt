package com.metoly.components.model

import com.metoly.components.RichTextEditorState
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.grid.DrawingStroke
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.GridItemFactory
import com.metoly.morganize.core.model.grid.NotePage

data class NoteEditorState(
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
    val isEraserMode: Boolean = false,
    val pages: List<NotePage> = listOf(GridItemFactory.createNotePage()),
    val selectedItemId: String? = null,
    val title: String = "",
    
    // ── Secret Note ─────────────────────────────────────────────────
    val isSecretNote: Boolean = false,
    val isSecretNoteUnlocked: Boolean = false,
    val transientSecretNotePassword: String? = null,
    val transientSecretNoteBiometric: Boolean = false,

    // ── Secret Item ─────────────────────────────────────────────────
    val unlockedItemIds: Set<String> = emptySet(),
    val transientDecryptedItems: Map<String, GridItem> = emptyMap(),
    val transientSecretItemKeys: Map<String, javax.crypto.SecretKey> = emptyMap(),

    // ── Unlock & Auth UI State ──────────────────────────────────────
    val showUnlockDialog: Boolean = false,
    val unlockTargetItemId: String? = null,
    val showSetCredentialsDialog: Boolean = false,
    val setCredentialsTargetItemId: String? = null,
    val pendingSecretItemWrappedItem: GridItem? = null
)
