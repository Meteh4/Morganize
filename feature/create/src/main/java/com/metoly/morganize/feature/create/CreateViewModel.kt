package com.metoly.morganize.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.create.model.CreateEvent
import com.metoly.morganize.feature.create.model.CreateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

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
            is CreateEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value) }
            }

            is CreateEvent.ContentChanged -> {
                _uiState.update { it.copy(content = event.value) }
            }

            is CreateEvent.BackgroundColorChanged -> {
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }
            }

            is CreateEvent.CategorySelected -> {
                _uiState.update { it.copy(categoryId = event.categoryId) }
            }

            is CreateEvent.ImageAdded -> {
                _uiState.update { it.copy(imagePaths = it.imagePaths + event.path) }
            }

            is CreateEvent.ImageRemoved -> {
                _uiState.update { it.copy(imagePaths = it.imagePaths - event.path) }
            }

            is CreateEvent.DrawingChanged -> {
                _uiState.update { it.copy(drawingPath = event.path) }
            }

            CreateEvent.MarkdownToggled -> {
                _uiState.update { it.copy(isMarkdownEnabled = !it.isMarkdownEnabled) }
            }

            is CreateEvent.ChecklistItemAdded -> {
                _uiState.update {
                    it.copy(
                        checklistItems = it.checklistItems + ChecklistItem(
                            text = event.text,
                            isChecked = false
                        )
                    )
                }
            }

            is CreateEvent.ChecklistItemToggled -> {
                _uiState.update {
                    val newItems = it.checklistItems.toMutableList()
                    val item = newItems[event.index]
                    newItems[event.index] = item.copy(isChecked = !item.isChecked)
                    it.copy(checklistItems = newItems)
                }
            }

            is CreateEvent.ChecklistItemRemoved -> {
                _uiState.update {
                    val newItems = it.checklistItems.toMutableList()
                    newItems.removeAt(event.index)
                    it.copy(checklistItems = newItems)
                }
            }

            is CreateEvent.ChecklistItemTextChanged -> {
                _uiState.update {
                    val newItems = it.checklistItems.toMutableList()
                    val item = newItems[event.index]
                    newItems[event.index] = item.copy(text = event.text)
                    it.copy(checklistItems = newItems)
                }
            }

            is CreateEvent.Save -> saveNote()
            is CreateEvent.NavigationHandled -> {
                _uiState.update { it.copy(isDone = false) }
            }

            is CreateEvent.SnackbarDismissed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun observeCategories() {
        categoryRepository
            .getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    _uiState.update { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveNote() {
        val currentState = _uiState.value
        val hasChecklist = currentState.checklistItems.isNotEmpty()
        if (currentState.title.isBlank() &&
            currentState.content.isBlank() &&
            currentState.imagePaths.isEmpty() &&
            currentState.drawingPath == null &&
            !hasChecklist
        )
            return

        val note =
            Note(
                title = currentState.title.trim(),
                content = currentState.content.trim(),
                backgroundColor = currentState.backgroundColor,
                categoryId = currentState.categoryId,
                imagePaths = currentState.imagePaths,
                drawingPath = currentState.drawingPath,
                isMarkdownEnabled = currentState.isMarkdownEnabled,
                checklistJson =
                    if (hasChecklist) Json.encodeToString(currentState.checklistItems)
                    else ""
            )

        noteRepository
            .insertNote(note)
            .onEach { state ->
                when (state) {
                    is ResponseState.Success -> {
                        _uiState.update { it.copy(isDone = true) }
                    }

                    is ResponseState.Error -> {
                        _uiState.update { it.copy(userMessage = state.message) }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}
