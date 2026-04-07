// CreateViewModel.kt
package com.metoly.morganize.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.addItemToLastPage
import com.metoly.components.model.removeItem
import com.metoly.components.model.updateItem
import com.metoly.components.model.updateItemSimple
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.grid.CheckboxEntry
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
import kotlin.collections.dropLast
import kotlin.collections.emptyList
import kotlin.collections.isNotEmpty

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
            is CreateEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.value) }

            is CreateEvent.AddPage ->
                _uiState.update { state ->
                    state.copy(pages = state.pages + NotePage(id = UUID.randomUUID().toString()))
                }

            is CreateEvent.ItemSelected ->
                _uiState.update { it.copy(selectedItemId = event.itemId) }

            is CreateEvent.ItemMoved ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Checklist -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is CreateEvent.ItemResized ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Text -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                                is GridItem.Checklist -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                            }
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

            is CreateEvent.TextGridItemRichSpansChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(richSpansJson = event.richSpansJson) else item
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

            is CreateEvent.TextGridItemAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.addItemToLastPage(
                            GridItem.Text(
                                id = UUID.randomUUID().toString(),
                                x = 0, y = 0, width = event.width, height = event.height,
                                textContent = event.text
                            )
                        )
                    )
                }

            is CreateEvent.ImageGridItemAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.addItemToLastPage(
                            GridItem.Image(
                                id = UUID.randomUUID().toString(),
                                x = 0, y = 0, width = event.width, height = event.height,
                                imageUri = event.path
                            )
                        )
                    )
                }

            is CreateEvent.ChecklistGridItemAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.addItemToLastPage(
                            GridItem.Checklist(
                                id = UUID.randomUUID().toString(),
                                x = 0, y = 0, width = event.width, height = event.height,
                                title = "",
                                entries = listOf(
                                    CheckboxEntry(id = UUID.randomUUID().toString())
                                )
                            )
                        )
                    )
                }

            is CreateEvent.ChecklistTitleChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) item.copy(title = event.title) else item
                        }
                    )
                }

            is CreateEvent.CheckboxToggled ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                item.copy(entries = item.entries.map { entry ->
                                    if (entry.id == event.entryId) entry.copy(isChecked = !entry.isChecked) else entry
                                })
                            } else item
                        }
                    )
                }

            is CreateEvent.CheckboxTextChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                item.copy(entries = item.entries.map { entry ->
                                    if (entry.id == event.entryId) entry.copy(text = event.text) else entry
                                })
                            } else item
                        }
                    )
                }

            is CreateEvent.CheckboxAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                item.copy(entries = item.entries + CheckboxEntry(id = UUID.randomUUID().toString()))
                            } else item
                        }
                    )
                }

            is CreateEvent.CheckboxDeleted ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Checklist) {
                                item.copy(entries = item.entries.filter { it.id != event.entryId })
                            } else item
                        }
                    )
                }

            is CreateEvent.ItemDeleted ->
                _uiState.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is CreateEvent.BackgroundColorChanged ->
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }

            is CreateEvent.CategorySelected ->
                _uiState.update { it.copy(categoryId = event.categoryId) }

            // ---- Rich text toolbar events ----

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

            // ── Drawing events ────────────────────────────────────────────

            is CreateEvent.DrawingModeToggled ->
                _uiState.update { state ->
                    state.copy(
                        isDrawingMode = !state.isDrawingMode,
                        isEraserMode = if (state.isDrawingMode) false else state.isEraserMode
                    )
                }

            is CreateEvent.DrawingColorChanged ->
                _uiState.update { it.copy(drawingPenColorArgb = event.colorArgb, isEraserMode = false) }

            is CreateEvent.DrawingStrokeWidthChanged ->
                _uiState.update { it.copy(drawingStrokeWidthFraction = event.widthFraction) }

            is CreateEvent.DrawingEraserWidthChanged ->
                _uiState.update { it.copy(drawingEraserWidthFraction = event.widthFraction) }

            is CreateEvent.DrawingEraserToggled ->
                _uiState.update { it.copy(isEraserMode = !it.isEraserMode) }

            is CreateEvent.DrawingStrokeAdded ->
                _uiState.update { state ->
                    val oldData = state.pages.find { it.id == event.pageId }?.drawingData ?: ""
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + oldData

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

                    if (pageStack.isEmpty()) {
                        return@update state.copy(
                            pages = state.pages.updateDrawingData(event.pageId) { current ->
                                if (current.isNotEmpty()) current.dropLast(1) else current
                            }
                        )
                    }

                    val previousData = pageStack.last()
                    newStack[event.pageId] = pageStack.dropLast(1)

                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { page ->
                            if (page.id == event.pageId) page.copy(drawingData = previousData) else page
                        }
                    )
                }

            is CreateEvent.DrawingStrokesUpdated ->
                _uiState.update { state ->
                    val oldData = state.pages.find { it.id == event.pageId }?.drawingData ?: ""
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + oldData

                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.updateDrawingData(event.pageId) { _ ->
                            event.strokes
                        }
                    )
                }

            // ---- Other events ----

            is CreateEvent.Save -> saveNote()

            is CreateEvent.NavigationHandled ->
                _uiState.update { it.copy(isDone = false) }

            is CreateEvent.SnackbarDismissed ->
                _uiState.update { it.copy(userMessage = null) }
        }
    }

    /**
     * Persist updated typography from editingRichState into the GridItem.Text in pages.
     * Called after font size / align / line height toolbar actions.
     */
    private fun persistTypography() {
        val state = _uiState.value
        val itemId = state.editingTextItemId ?: return
        val rs = state.editingRichState ?: return
        _uiState.update { s ->
            s.copy(
                pages = s.pages.map { page ->
                    page.copy(
                        items = page.items.map { item ->
                            if (item is GridItem.Text && item.id == itemId) {
                                item.copy(
                                    fontSize = rs.fontSize,
                                    textAlign = rs.textAlign,
                                    lineHeight = rs.lineHeight
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

// ────────────────────────────────────────────────────────────────────────────
// Extension helpers
// ────────────────────────────────────────────────────────────────────────────

/**
 * Updates the drawing stroke list of the page with the given [pageId] by applying
 * the [transform] function. Pages with other IDs are left unchanged.
 */
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
    val serialized = if (updated.isEmpty()) ""
    else Json.encodeToString(updated)
    page.copy(drawingData = serialized)
}