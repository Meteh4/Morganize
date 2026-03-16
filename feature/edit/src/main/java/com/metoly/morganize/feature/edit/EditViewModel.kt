package com.metoly.morganize.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        categoryRepository
                .getAllCategories()
                .onEach { state ->
                    if (state is ResponseState.Success) {
                        _uiState.update { it.copy(categories = state.data ?: emptyList()) }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun onEvent(event: EditEvent) {
        when (event) {
            is EditEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value) }
            }
            is EditEvent.ContentChanged -> {
                _uiState.update { it.copy(content = event.value) }
            }
            is EditEvent.BackgroundColorChanged -> {
                _uiState.update { it.copy(backgroundColor = event.colorArgb) }
            }
            is EditEvent.CategorySelected -> {
                _uiState.update { it.copy(categoryId = event.categoryId) }
            }
            is EditEvent.ImageAdded -> {
                _uiState.update { it.copy(imagePaths = it.imagePaths + event.path) }
            }
            is EditEvent.ImageRemoved -> {
                _uiState.update { it.copy(imagePaths = it.imagePaths - event.path) }
            }
            is EditEvent.DrawingChanged -> {
                _uiState.update { it.copy(drawingPath = event.path) }
            }
            is EditEvent.MarkdownToggled -> {
                _uiState.update { it.copy(isMarkdownEnabled = !it.isMarkdownEnabled) }
            }
            is EditEvent.ChecklistItemAdded -> {
                val newList = _uiState.value.checklistItems + ChecklistItem(text = event.text, isChecked = false)
                _uiState.update { it.copy(checklistItems = newList) }
            }
            is EditEvent.ChecklistItemToggled -> {
                val newList = _uiState.value.checklistItems.toMutableList()
                val item = newList[event.index]
                newList[event.index] = item.copy(isChecked = !item.isChecked)
                _uiState.update { it.copy(checklistItems = newList) }
            }
            is EditEvent.ChecklistItemTextChanged -> {
                val newList = _uiState.value.checklistItems.toMutableList()
                val item = newList[event.index]
                newList[event.index] = item.copy(text = event.text)
                _uiState.update { it.copy(checklistItems = newList) }
            }
            is EditEvent.ChecklistItemRemoved -> {
                val newList = _uiState.value.checklistItems.toMutableList()
                newList.removeAt(event.index)
                _uiState.update { it.copy(checklistItems = newList) }
            }
            is EditEvent.Save -> saveNote()
            is EditEvent.DeleteRequested -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
            is EditEvent.DeleteConfirmed -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
                deleteNote()
            }
            is EditEvent.DeleteDismissed -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
            }
            is EditEvent.NavigationHandled -> {
                _uiState.update { it.copy(isDone = false) }
            }
            is EditEvent.SnackbarDismissed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun loadNote() {
        noteRepository
                .getNoteById(noteId)
                .onEach { state ->
                    when (state) {
                        ResponseState.Loading -> {
                            _uiState.update { it.copy(noteState = ResponseState.Loading) }
                        }
                        is ResponseState.Success -> {
                            val note = state.data
                            if (note != null) {
                                originalNote = note
                                val checklist =
                                        try {
                                            if (note.checklistJson.isNotEmpty()) {
                                                Json.decodeFromString<List<ChecklistItem>>(
                                                        note.checklistJson
                                                )
                                            } else emptyList()
                                        } catch (e: Exception) {
                                            emptyList()
                                        }

                                _uiState.update {
                                    it.copy(
                                            title = note.title,
                                            content = note.content,
                                            backgroundColor = note.backgroundColor,
                                            categoryId = note.categoryId,
                                            imagePaths = note.imagePaths,
                                            drawingPath = note.drawingPath,
                                            isMarkdownEnabled = note.isMarkdownEnabled,
                                            checklistItems = checklist,
                                            noteState = ResponseState.Success(Unit)
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(noteState = ResponseState.Error("Not bulunamadı"))
                                }
                            }
                        }
                        is ResponseState.Error -> {
                            _uiState.update {
                                it.copy(noteState = ResponseState.Error(state.message))
                            }
                        }
                        ResponseState.Idle -> Unit
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun saveNote() {
        val current = originalNote ?: return
        val checklistJson =
                if (_uiState.value.checklistItems.isNotEmpty()) {
                    Json.encodeToString(_uiState.value.checklistItems)
                } else ""

        val updatedNote =
                current.copy(
                        title = _uiState.value.title.trim(),
                        content = _uiState.value.content.trim(),
                        backgroundColor = _uiState.value.backgroundColor,
                        categoryId = _uiState.value.categoryId,
                        imagePaths = _uiState.value.imagePaths,
                        drawingPath = _uiState.value.drawingPath,
                        isMarkdownEnabled = _uiState.value.isMarkdownEnabled,
                        checklistJson = checklistJson,
                        updatedAt = System.currentTimeMillis()
                )

        noteRepository
                .updateNote(updatedNote)
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

    private fun deleteNote() {
        val current = originalNote ?: return
        noteRepository
                .deleteNote(current)
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
