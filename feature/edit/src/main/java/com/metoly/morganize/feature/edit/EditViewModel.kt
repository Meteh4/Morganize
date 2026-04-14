package com.metoly.morganize.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.addItemToPage
import com.metoly.components.model.removeItem
import com.metoly.components.model.updateItem
import com.metoly.components.model.updateItemSimple
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.grid.CheckboxEntry
import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.NotePage
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.UUID

class EditViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private var originalNote: Note? = null

    init {
        loadCategories()
        loadNote()
    }

    fun onEvent(event: EditEvent) {
        when (event) {
            is EditEvent.AddPage ->
                _uiState.update { state ->
                    state.copy(pages = state.pages + NotePage(id = UUID.randomUUID().toString()))
                }

            is EditEvent.BackgroundColorChanged ->
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }

            is EditEvent.CategorySelected ->
                _uiState.update { it.copy(categoryId = event.categoryId) }

            is EditEvent.ChecklistGridItemAdded,
            is EditEvent.ChecklistAction -> handleChecklist(event)

            is EditEvent.DeleteConfirmed -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
                deleteNote()
            }

            is EditEvent.DeleteDismissed ->
                _uiState.update { it.copy(showDeleteDialog = false) }

            is EditEvent.DeleteRequested ->
                _uiState.update { it.copy(showDeleteDialog = true) }

            is EditEvent.DrawingModeToggled,
            is EditEvent.DrawingColorChanged,
            is EditEvent.DrawingStrokeWidthChanged,
            is EditEvent.DrawingEraserWidthChanged,
            is EditEvent.DrawingEraserToggled,
            is EditEvent.DrawingStrokeAdded,
            is EditEvent.DrawingStrokeReverted,
            is EditEvent.DrawingStrokesUpdated -> handleDrawing(event)

            is EditEvent.EditingTextItemChanged,
            is EditEvent.RichStateUpdated -> handleRichText(event)

            is EditEvent.ImageGridItemAdded -> handleImageGridItem(event)

            is EditEvent.ItemSelected,
            is EditEvent.ItemMoved,
            is EditEvent.ItemResized,
            is EditEvent.ItemDeleted -> handleGridItem(event)

            is EditEvent.NavigationHandled ->
                _uiState.update { it.copy(isDone = false) }

            is EditEvent.Save -> saveNote()

            is EditEvent.SnackbarDismissed ->
                _uiState.update { it.copy(userMessage = null) }

            is EditEvent.TextGridItemTextChanged,
            is EditEvent.TextGridItemRichSpansChanged,
            is EditEvent.TextGridItemTypographyChanged,
            is EditEvent.TextGridItemAdded -> handleTextGridItem(event)

            is EditEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.value) }

            is EditEvent.ScrollTargetHandled ->
                _uiState.update { it.copy(targetScrollPageIndex = null) }
        }
    }

    private fun handleGridItem(event: EditEvent) {
        when (event) {
            is EditEvent.ItemDeleted ->
                _uiState.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is EditEvent.ItemMoved ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId,
                            event.itemId,
                            revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is EditEvent.ItemResized ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId,
                            event.itemId,
                            revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(
                                    width = event.newWidth,
                                    height = event.newHeight,
                                    x = event.newX,
                                    y = event.newY
                                )

                                is GridItem.Image -> item.copy(
                                    width = event.newWidth,
                                    height = event.newHeight,
                                    x = event.newX,
                                    y = event.newY
                                )

                                is GridItem.Text -> item.copy(
                                    width = event.newWidth,
                                    height = event.newHeight,
                                    x = event.newX,
                                    y = event.newY
                                )
                            }
                        }
                    )
                }

            is EditEvent.ItemSelected ->
                _uiState.update { it.copy(selectedItemId = event.itemId) }

            else -> Unit
        }
    }

    private fun handleTextGridItem(event: EditEvent) {
        when (event) {
            is EditEvent.TextGridItemAdded ->
                _uiState.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItem.Text(
                            id = UUID.randomUUID().toString(),
                            x = 0, y = 0, width = event.width, height = event.height,
                            textContent = event.text
                        )
                    )
                    state.copy(
                        pages = newPages,
                        targetScrollPageIndex = addedIndex
                    )
                }

            is EditEvent.TextGridItemRichSpansChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(richSpans = event.richSpans) else item
                        }
                    )
                }

            is EditEvent.TextGridItemTextChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(textContent = event.text) else item
                        }
                    )
                }

            is EditEvent.TextGridItemTypographyChanged ->
                _uiState.update { state ->
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

    private fun handleImageGridItem(event: EditEvent.ImageGridItemAdded) {
        _uiState.update { state ->
            val (newPages, addedIndex) = state.pages.addItemToPage(
                event.targetPageIndex,
                GridItem.Image(
                    id = UUID.randomUUID().toString(),
                    x = 0, y = 0, width = event.width, height = event.height,
                    imageUri = event.path
                )
            )
            state.copy(
                pages = newPages,
                targetScrollPageIndex = addedIndex
            )
        }
    }

    private fun handleChecklist(event: EditEvent) {
        when (event) {
            is EditEvent.ChecklistAction ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                applyChecklistAction(item, event.action)
                            } else item
                        }
                    )
                }

            is EditEvent.ChecklistGridItemAdded ->
                _uiState.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItem.Checklist(
                            id = UUID.randomUUID().toString(),
                            x = 0, y = 0, width = event.width, height = event.height,
                            title = "",
                            entries = listOf(
                                CheckboxEntry(id = UUID.randomUUID().toString())
                            )
                        )
                    )
                    state.copy(
                        pages = newPages,
                        targetScrollPageIndex = addedIndex
                    )
                }

            else -> Unit
        }
    }

    private fun applyChecklistAction(
        item: GridItem.Checklist,
        action: ChecklistActionType
    ): GridItem.Checklist = when (action) {
        is ChecklistActionType.EntryAdded ->
            item.copy(entries = item.entries + CheckboxEntry(id = UUID.randomUUID().toString()))

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

    private fun handleRichText(event: EditEvent) {
        when (event) {
            is EditEvent.EditingTextItemChanged ->
                _uiState.update { it.copy(editingTextItemId = event.itemId) }

            is EditEvent.RichStateUpdated -> {
                val oldState = _uiState.value.editingRichState
                _uiState.update { it.copy(editingRichState = event.state) }
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

    private fun handleDrawing(event: EditEvent) {
        when (event) {
            is EditEvent.DrawingColorChanged ->
                _uiState.update {
                    it.copy(
                        drawingPenColorArgb = event.colorArgb,
                        isEraserMode = false
                    )
                }

            is EditEvent.DrawingEraserToggled ->
                _uiState.update { it.copy(isEraserMode = !it.isEraserMode) }

            is EditEvent.DrawingEraserWidthChanged ->
                _uiState.update { it.copy(drawingEraserWidthFraction = event.widthFraction) }

            is EditEvent.DrawingModeToggled ->
                _uiState.update { state ->
                    state.copy(
                        isDrawingMode = !state.isDrawingMode,
                        isEraserMode = if (state.isDrawingMode) false else state.isEraserMode
                    )
                }

            is EditEvent.DrawingStrokeAdded ->
                _uiState.update { state ->
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

            is EditEvent.DrawingStrokeReverted ->
                _uiState.update { state ->
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

            is EditEvent.DrawingStrokesUpdated ->
                _uiState.update { state ->
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

            is EditEvent.DrawingStrokeWidthChanged ->
                _uiState.update { it.copy(drawingStrokeWidthFraction = event.widthFraction) }

            else -> Unit
        }
    }

    private fun persistTypography() {
        val state = _uiState.value
        val itemId = state.editingTextItemId ?: return
        val richState = state.editingRichState ?: return
        _uiState.update { currentState ->
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

    private fun loadCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    _uiState.update { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadNote() {
        noteRepository.getNoteById(noteId)
            .onEach { state ->
                when (state) {
                    ResponseState.Loading ->
                        _uiState.update { it.copy(noteState = ResponseState.Loading) }

                    is ResponseState.Success -> {
                        val note = state.data
                        if (note != null) {
                            originalNote = note
                            val loadedPages = runCatching {
                                if (note.pagesJson.isNotEmpty())
                                    kotlinx.serialization.json.Json.decodeFromString<List<NotePage>>(note.pagesJson)
                                else null
                            }.getOrNull() ?: listOf(NotePage(id = UUID.randomUUID().toString()))
                            _uiState.update {
                                it.copy(
                                    title = note.title,
                                    pages = loadedPages,
                                    backgroundColor = note.backgroundColor,
                                    categoryId = note.categoryId,
                                    noteState = ResponseState.Success(Unit)
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(noteState = ResponseState.Error("Note not found"))
                            }
                        }
                    }

                    is ResponseState.Error ->
                        _uiState.update { it.copy(noteState = ResponseState.Error(state.message)) }

                    ResponseState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveNote() {
        val current = originalNote ?: return
        val state = _uiState.value
        val updatedNote = current.copy(
            title = state.title.trim(),
            pagesJson = kotlinx.serialization.json.Json.encodeToString(state.pages),
            backgroundColor = state.backgroundColor,
            categoryId = state.categoryId,
            updatedAt = System.currentTimeMillis()
        )
        noteRepository.updateNote(updatedNote)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> _uiState.update { it.copy(isDone = true) }
                    is ResponseState.Error -> _uiState.update { it.copy(userMessage = result.message) }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun deleteNote() {
        val current = originalNote ?: return
        noteRepository.deleteNote(current)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> _uiState.update { it.copy(isDone = true) }
                    is ResponseState.Error -> _uiState.update { it.copy(userMessage = result.message) }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}