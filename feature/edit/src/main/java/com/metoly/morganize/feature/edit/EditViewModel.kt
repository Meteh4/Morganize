// EditViewModel.kt
package com.metoly.morganize.feature.edit

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
import kotlinx.serialization.json.Json
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

    private fun loadCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    _uiState.update { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: EditEvent) {
        when (event) {
            is EditEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.value) }

            is EditEvent.AddPage ->
                _uiState.update { state ->
                    state.copy(pages = state.pages + NotePage(id = UUID.randomUUID().toString()))
                }

            is EditEvent.ItemSelected ->
                _uiState.update { it.copy(selectedItemId = event.itemId) }

            is EditEvent.ItemMoved ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is EditEvent.ItemResized ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(event.pageId, event.itemId, revertOnOverlap = true) { item ->
                            when (item) {
                                is GridItem.Text -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(width = event.newWidth, height = event.newHeight, x = event.newX, y = event.newY)
                            }
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

            is EditEvent.TextGridItemRichSpansChanged ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                            if (item is GridItem.Text) item.copy(richSpansJson = event.richSpansJson) else item
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

            is EditEvent.TextGridItemAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.addItemToLastPage(
                            GridItem.Text(
                                id = UUID.randomUUID().toString(),
                                x = 0, y = 0, width = 4, height = 4,
                                textContent = event.text
                            )
                        )
                    )
                }

            is EditEvent.ImageGridItemAdded ->
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.addItemToLastPage(
                            GridItem.Image(
                                id = UUID.randomUUID().toString(),
                                x = 0, y = 0, width = 6, height = 6,
                                imageUri = event.path
                            )
                        )
                    )
                }

            is EditEvent.ItemDeleted ->
                _uiState.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is EditEvent.BackgroundColorChanged ->
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }

            is EditEvent.CategorySelected ->
                _uiState.update { it.copy(categoryId = event.categoryId) }

            // ---- Rich text toolbar events ----

            is EditEvent.EditingTextItemChanged ->
                _uiState.update { it.copy(editingTextItemId = event.itemId) }

            is EditEvent.RichStateUpdated -> {
                val oldState = _uiState.value.editingRichState
                _uiState.update { it.copy(editingRichState = event.state) }
                if (oldState != null && 
                    (oldState.fontSize != event.state.fontSize || 
                     oldState.textAlign != event.state.textAlign || 
                     oldState.lineHeight != event.state.lineHeight)) {
                    persistTypography()
                }
            }

            is EditEvent.Save -> saveNote()

            is EditEvent.DeleteRequested ->
                _uiState.update { it.copy(showDeleteDialog = true) }

            is EditEvent.DeleteConfirmed -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
                deleteNote()
            }

            is EditEvent.DeleteDismissed ->
                _uiState.update { it.copy(showDeleteDialog = false) }

            is EditEvent.NavigationHandled ->
                _uiState.update { it.copy(isDone = false) }

            is EditEvent.SnackbarDismissed ->
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
                                    Json.decodeFromString<List<NotePage>>(note.pagesJson)
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
            pagesJson = Json.encodeToString(state.pages),
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