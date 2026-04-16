package com.metoly.morganize.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.NoteEditorDelegate
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.edit.model.EditSpecificState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class EditViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val delegate = NoteEditorDelegate()

    val uiState = delegate.state
    val uiEvent = delegate.uiEvent

    private val _editState = MutableStateFlow(EditSpecificState())
    val editState: StateFlow<EditSpecificState> = _editState.asStateFlow()

    private var originalNote: Note? = null

    init {
        loadCategories()
        loadNote()
    }

    fun requestDelete() {
        _editState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDelete() {
        _editState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        _editState.update { it.copy(showDeleteDialog = false) }
        deleteNote()
    }

    fun save() {
        val current = originalNote ?: return
        val state = delegate.state.value
        val updatedNote = current.copy(
            title = state.title.trim(),
            pages = state.pages,
            backgroundColor = state.backgroundColor,
            categoryId = state.categoryId,
            updatedAt = System.currentTimeMillis()
        )
        noteRepository.updateNote(updatedNote)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                    is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    delegate.updateState { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadNote() {
        noteRepository.getNoteById(noteId)
            .onEach { state ->
                when (state) {
                    ResponseState.Loading ->
                        _editState.update { it.copy(noteState = ResponseState.Loading) }

                    is ResponseState.Success -> {
                        val note = state.data
                        if (note != null) {
                            originalNote = note
                            val loadedPages = note.pages.ifEmpty { 
                                listOf(com.metoly.morganize.core.model.grid.GridItemFactory.createNotePage()) 
                            }
                            delegate.updateState {
                                it.copy(
                                    title = note.title,
                                    pages = loadedPages,
                                    backgroundColor = note.backgroundColor,
                                    categoryId = note.categoryId
                                )
                            }
                            _editState.update { it.copy(noteState = ResponseState.Success(Unit)) }
                        } else {
                            _editState.update {
                                it.copy(noteState = ResponseState.Error("Note not found"))
                            }
                        }
                    }

                    is ResponseState.Error ->
                        _editState.update { it.copy(noteState = ResponseState.Error(state.message)) }

                    ResponseState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun deleteNote() {
        val current = originalNote ?: return
        noteRepository.deleteNote(current)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                    is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}