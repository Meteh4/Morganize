package com.metoly.morganize.feature.create

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
import com.metoly.morganize.core.model.grid.DrawingStroke
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.NotePage
import com.metoly.morganize.feature.create.model.CreateEvent
import com.metoly.morganize.feature.create.model.CreateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.util.UUID

class CreateViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    fun onEvent(event: CreateEvent) {
        when (event) {
            is CreateEvent.AddPage ->
                _uiState.update { state ->
                    state.copy(pages = state.pages + NotePage(id = UUID.randomUUID().toString()))
                }

            is CreateEvent.BackgroundColorChanged ->
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }

            is CreateEvent.CategorySelected ->
                _uiState.update { it.copy(categoryId = event.categoryId) }

            is CreateEvent.ChecklistGridItemAdded,
            is CreateEvent.ChecklistAction -> handleChecklist(event)

            is CreateEvent.DrawingModeToggled,
            is CreateEvent.DrawingColorChanged,
            is CreateEvent.DrawingStrokeWidthChanged,
            is CreateEvent.DrawingEraserWidthChanged,
            is CreateEvent.DrawingEraserToggled,
            is CreateEvent.DrawingStrokeAdded,
            is CreateEvent.DrawingStrokeReverted,
            is CreateEvent.DrawingStrokesUpdated -> handleDrawing(event)

            is CreateEvent.EditingTextItemChanged,
            is CreateEvent.RichStateUpdated -> handleRichText(event)

            is CreateEvent.ImageGridItemAdded -> handleImageGridItem(event)

            is CreateEvent.ItemSelected,
            is CreateEvent.ItemMoved,
            is CreateEvent.ItemResized,
            is CreateEvent.ItemDeleted -> handleGridItem(event)

            is CreateEvent.NavigationHandled ->
                _uiState.update { it.copy(isDone = false) }

            is CreateEvent.Save -> saveNote()

            is CreateEvent.SnackbarDismissed ->
                _uiState.update { it.copy(userMessage = null) }

            is CreateEvent.TextGridItemTextChanged,
            is CreateEvent.TextGridItemRichSpansChanged,
            is CreateEvent.TextGridItemTypographyChanged,
            is CreateEvent.TextGridItemAdded -> handleTextGridItem(event)

            is CreateEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.value) }

            is CreateEvent.ScrollTargetHandled ->
                _uiState.update { it.copy(targetScrollPageIndex = null) }
        }
    }

    private fun handleGridItem(event: CreateEvent) {
        when (event) {
            is CreateEvent.ItemDeleted ->
                _uiState.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is CreateEvent.ItemMoved ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is CreateEvent.ItemResized ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                                is GridItem.Text -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is CreateEvent.ItemSelected ->
                _uiState.update { it.copy(selectedItemId = event.itemId) }

            else -> Unit
        }
    }

    private fun handleTextGridItem(event: CreateEvent) {
        when (event) {
            is CreateEvent.TextGridItemAdded ->
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

            is CreateEvent.TextGridItemRichSpansChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(richSpansJson = event.richSpansJson) else item
                        }
                    )
                }

            is CreateEvent.TextGridItemTextChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(textContent = event.text) else item
                        }
                    )
                }

            is CreateEvent.TextGridItemTypographyChanged ->
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

    private fun handleImageGridItem(event: CreateEvent.ImageGridItemAdded) {
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

    private fun handleChecklist(event: CreateEvent) {
        when (event) {
            is CreateEvent.ChecklistAction ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                applyChecklistAction(item, event.action)
                            } else item
                        }
                    )
                }

            is CreateEvent.ChecklistGridItemAdded ->
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

    private fun handleRichText(event: CreateEvent) {
        when (event) {
            is CreateEvent.EditingTextItemChanged ->
                _uiState.update { it.copy(editingTextItemId = event.itemId) }

            is CreateEvent.RichStateUpdated -> {
                val oldState = _uiState.value.editingRichState
                _uiState.update { it.copy(editingRichState = event.state) }
                if (oldState != null &&
                    (oldState.fontSize != event.state.fontSize ||
                            oldState.textAlign != event.state.textAlign ||
                            oldState.lineHeight != event.state.lineHeight)) {
                    persistTypography()
                }
            }

            else -> Unit
        }
    }

    private fun handleDrawing(event: CreateEvent) {
        when (event) {
            is CreateEvent.DrawingColorChanged ->
                _uiState.update { it.copy(drawingPenColorArgb = event.colorArgb, isEraserMode = false) }

            is CreateEvent.DrawingEraserToggled ->
                _uiState.update { it.copy(isEraserMode = !it.isEraserMode) }

            is CreateEvent.DrawingEraserWidthChanged ->
                _uiState.update { it.copy(drawingEraserWidthFraction = event.widthFraction) }

            is CreateEvent.DrawingModeToggled ->
                _uiState.update { state ->
                    state.copy(
                        isDrawingMode = !state.isDrawingMode,
                        isEraserMode = if (state.isDrawingMode) false else state.isEraserMode
                    )
                }

            is CreateEvent.DrawingStrokeAdded ->
                _uiState.update { state ->
                    val currentStrokes = state.pages.decodeStrokes(event.pageId)
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.updateDrawingData(event.pageId) { current ->
                            current + event.stroke
                        }
                    )
                }

            is CreateEvent.DrawingStrokeReverted ->
                _uiState.update { state ->
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    if (pageStack.isEmpty()) return@update state
                    val previousStrokes = pageStack.last()
                    newStack[event.pageId] = pageStack.dropLast(1)
                    val serialized = if (previousStrokes.isEmpty()) "" else Json.encodeToString(previousStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { page ->
                            if (page.id == event.pageId) page.copy(drawingData = serialized) else page
                        }
                    )
                }

            is CreateEvent.DrawingStrokesUpdated ->
                _uiState.update { state ->
                    val currentStrokes = state.pages.decodeStrokes(event.pageId)
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.updateDrawingData(event.pageId) { _ ->
                            event.strokes
                        }
                    )
                }

            is CreateEvent.DrawingStrokeWidthChanged ->
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

    private fun observeCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    _uiState.update { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank() && state.pages.all { it.items.isEmpty() }) return
        val note = Note(
            title = state.title.trim(),
            pagesJson = Json.encodeToString(state.pages),
            backgroundColor = state.backgroundColor,
            categoryId = state.categoryId
        )
        noteRepository.insertNote(note)
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

private fun List<NotePage>.decodeStrokes(pageId: String): List<DrawingStroke> {
    val page = find { it.id == pageId } ?: return emptyList()
    if (page.drawingData.isBlank()) return emptyList()
    return runCatching {
        Json.decodeFromString<List<DrawingStroke>>(page.drawingData)
    }.getOrDefault(emptyList())
}

private fun List<NotePage>.updateDrawingData(
    pageId: String,
    transform: (List<DrawingStroke>) -> List<DrawingStroke>
): List<NotePage> = map { page ->
    if (page.id != pageId) return@map page
    val current = if (page.drawingData.isBlank()) emptyList()
    else runCatching {
        Json.decodeFromString<List<DrawingStroke>>(page.drawingData)
    }.getOrDefault(emptyList())
    val updated = transform(current)
    val serialized = if (updated.isEmpty()) "" else Json.encodeToString(updated)
    page.copy(drawingData = serialized)
}