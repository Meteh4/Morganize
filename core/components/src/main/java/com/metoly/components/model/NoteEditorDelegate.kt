package com.metoly.components.model

import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.GridItemFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * UI'dan bağımsız state holder. Grid, Drawing, Text, Checklist ve RichText
 * işlemlerinin tümünü yönetir. ViewModel'lar bu sınıfı delegation ile kullanır.
 */
class NoteEditorDelegate(
    private val onCategoryCreate: (name: String, colorArgb: Int) -> Unit = { _, _ -> }
) {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private val _uiEvent = Channel<NoteEditorUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    /** ViewModel'ın doğrudan state'i güncellemesi gerektiğinde (ör. loadNote). */
    fun updateState(transform: (NoteEditorState) -> NoteEditorState) {
        _state.update(transform)
    }

    /** One-time UI event gönder. */
    fun sendUiEvent(event: NoteEditorUiEvent) {
        _uiEvent.trySend(event)
    }

    /** Ana event router. */
    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.AddPage ->
                _state.update { state ->
                    state.copy(pages = state.pages + GridItemFactory.createNotePage())
                }

            is NoteEditorEvent.BackgroundColorChanged ->
                _state.update { it.copy(backgroundColor = event.colorArgb) }

            is NoteEditorEvent.CategorySelected ->
                _state.update { it.copy(categoryId = event.categoryId) }

            is NoteEditorEvent.ChecklistGridItemAdded,
            is NoteEditorEvent.ChecklistAction -> handleChecklist(event)

            is NoteEditorEvent.DrawingModeToggled,
            is NoteEditorEvent.DrawingColorChanged,
            is NoteEditorEvent.DrawingStrokeWidthChanged,
            is NoteEditorEvent.DrawingEraserWidthChanged,
            is NoteEditorEvent.DrawingEraserToggled,
            is NoteEditorEvent.DrawingStrokeAdded,
            is NoteEditorEvent.DrawingStrokeReverted,
            is NoteEditorEvent.DrawingStrokesUpdated -> handleDrawing(event)

            is NoteEditorEvent.EditingTextItemChanged,
            is NoteEditorEvent.RichStateUpdated -> handleRichText(event)

            is NoteEditorEvent.ImageGridItemAdded -> handleImageGridItem(event)

            is NoteEditorEvent.ItemSelected,
            is NoteEditorEvent.ItemMoved,
            is NoteEditorEvent.ItemResized,
            is NoteEditorEvent.ItemDeleted -> handleGridItem(event)

            is NoteEditorEvent.TextGridItemTextChanged,
            is NoteEditorEvent.TextGridItemRichSpansChanged,
            is NoteEditorEvent.TextGridItemTypographyChanged,
            is NoteEditorEvent.TextGridItemAdded -> handleTextGridItem(event)

            is NoteEditorEvent.TitleChanged ->
                _state.update { it.copy(title = event.value) }

            is NoteEditorEvent.CreateCategory -> {
                onCategoryCreate(event.name, event.colorArgb)
            }
        }
    }

    // ── Grid Item ────────────────────────────────────────────────────────

    private fun handleGridItem(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ItemDeleted ->
                _state.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is NoteEditorEvent.ItemMoved ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId, event.itemId, revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is NoteEditorEvent.ItemResized ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId, event.itemId, revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                                is GridItem.Image -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                                is GridItem.Text -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                            }
                        }
                    )
                }

            is NoteEditorEvent.ItemSelected ->
                _state.update { it.copy(selectedItemId = event.itemId) }

            else -> Unit
        }
    }

    // ── Text Grid Item ───────────────────────────────────────────────────

    private fun handleTextGridItem(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TextGridItemAdded ->
                _state.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItemFactory.createTextItem(
                            width = event.width, height = event.height,
                            textContent = event.text
                        )
                    )
                    _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
                    state.copy(pages = newPages)
                }

            is NoteEditorEvent.TextGridItemRichSpansChanged ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(richSpans = event.richSpans) else item
                        }
                    )
                }

            is NoteEditorEvent.TextGridItemTextChanged ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(textContent = event.text) else item
                        }
                    )
                }

            is NoteEditorEvent.TextGridItemTypographyChanged ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(
                                fontSize = event.fontSize,
                                textAlign = event.textAlign,
                                lineHeight = event.lineHeight
                            ) else item
                        }
                    )
                }

            else -> Unit
        }
    }

    // ── Image Grid Item ──────────────────────────────────────────────────

    private fun handleImageGridItem(event: NoteEditorEvent.ImageGridItemAdded) {
        _state.update { state ->
            val (newPages, addedIndex) = state.pages.addItemToPage(
                event.targetPageIndex,
                GridItemFactory.createImageItem(
                    width = event.width, height = event.height,
                    imageUri = event.path
                )
            )
            _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
            state.copy(pages = newPages)
        }
    }

    // ── Checklist ────────────────────────────────────────────────────────

    private fun handleChecklist(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ChecklistAction ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                applyChecklistAction(item, event.action)
                            } else item
                        }
                    )
                }

            is NoteEditorEvent.ChecklistGridItemAdded ->
                _state.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItemFactory.createChecklistItem(
                            width = event.width, height = event.height
                        )
                    )
                    _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
                    state.copy(pages = newPages)
                }

            else -> Unit
        }
    }

    private fun applyChecklistAction(
        item: GridItem.Checklist,
        action: ChecklistActionType
    ): GridItem.Checklist = when (action) {
        is ChecklistActionType.EntryAdded ->
            item.copy(entries = item.entries + GridItemFactory.createCheckboxEntry())

        is ChecklistActionType.EntryDeleted ->
            item.copy(entries = item.entries.filter { it.id != action.entryId })

        is ChecklistActionType.EntryTextChanged ->
            item.copy(entries = item.entries.map { entry ->
                if (entry.id == action.entryId) entry.copy(text = action.text) else entry
            })

        is ChecklistActionType.EntryToggled ->
            item.copy(entries = item.entries.map { entry ->
                if (entry.id == action.entryId) entry.copy(isChecked = !entry.isChecked) else entry
            })

        is ChecklistActionType.TitleChanged ->
            item.copy(title = action.title)
    }

    // ── Rich Text ────────────────────────────────────────────────────────

    private fun handleRichText(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.EditingTextItemChanged ->
                _state.update { it.copy(editingTextItemId = event.itemId) }

            is NoteEditorEvent.RichStateUpdated -> {
                val oldState = _state.value.editingRichState
                _state.update { it.copy(editingRichState = event.state) }
                if (oldState != null &&
                    (oldState.fontSize != event.state.fontSize ||
                            oldState.textAlign != event.state.textAlign ||
                            oldState.lineHeight != event.state.lineHeight)
                ) {
                    persistTypography()
                }
            }

            else -> Unit
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────

    private fun handleDrawing(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.DrawingColorChanged ->
                _state.update { it.copy(drawingPenColorArgb = event.colorArgb, isEraserMode = false) }

            is NoteEditorEvent.DrawingEraserToggled ->
                _state.update { it.copy(isEraserMode = !it.isEraserMode) }

            is NoteEditorEvent.DrawingEraserWidthChanged ->
                _state.update { it.copy(drawingEraserWidthFraction = event.widthFraction) }

            is NoteEditorEvent.DrawingModeToggled ->
                _state.update { state ->
                    state.copy(
                        isDrawingMode = !state.isDrawingMode,
                        isEraserMode = if (state.isDrawingMode) false else state.isEraserMode
                    )
                }

            is NoteEditorEvent.DrawingStrokeAdded ->
                _state.update { state ->
                    val page = state.pages.find { it.id == event.pageId }
                    val currentStrokes = page?.strokes ?: emptyList()
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { p ->
                            if (p.id == event.pageId) p.copy(strokes = currentStrokes + event.stroke) else p
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokeReverted ->
                _state.update { state ->
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    if (pageStack.isEmpty()) return@update state
                    val previousStrokes = pageStack.last()
                    newStack[event.pageId] = pageStack.dropLast(1)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { page ->
                            if (page.id == event.pageId) page.copy(strokes = previousStrokes) else page
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokesUpdated ->
                _state.update { state ->
                    val page = state.pages.find { it.id == event.pageId }
                    val currentStrokes = page?.strokes ?: emptyList()
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { p ->
                            if (p.id == event.pageId) p.copy(strokes = event.strokes) else p
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokeWidthChanged ->
                _state.update { it.copy(drawingStrokeWidthFraction = event.widthFraction) }

            else -> Unit
        }
    }

    // ── Persist Typography ───────────────────────────────────────────────

    private fun persistTypography() {
        val state = _state.value
        val itemId = state.editingTextItemId ?: return
        val richState = state.editingRichState ?: return
        _state.update { currentState ->
            currentState.copy(
                pages = currentState.pages.map { page ->
                    page.copy(
                        items = page.items.map { item ->
                            if (item is GridItem.Text && item.id == itemId) {
                                item.copy(
                                    fontSize = richState.fontSize,
                                    textAlign = richState.textAlign,
                                    lineHeight = richState.lineHeight
                                )
                            } else item
                        }
                    )
                }
            )
        }
    }
}
