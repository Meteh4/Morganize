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
                                x = 0, y = 0, width = 4, height = 4,
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
                                x = 0, y = 0, width = 6, height = 6,
                                imageUri = event.path
                            )
                        )
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