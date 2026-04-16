package com.metoly.morganize.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.NoteEditorDelegate
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreateViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val delegate = NoteEditorDelegate()

    val uiState = delegate.state
    val uiEvent = delegate.uiEvent

    init {
        observeCategories()
    }

    fun save() {
        val state = delegate.state.value
        if (state.title.isBlank() && state.pages.all { it.items.isEmpty() }) return
        val note = Note(
            title = state.title.trim(),
            pages = state.pages,
            backgroundColor = state.backgroundColor,
            categoryId = state.categoryId
        )
        noteRepository.insertNote(note)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                    is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    delegate.updateState { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }
}