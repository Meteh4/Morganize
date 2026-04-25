package com.metoly.morganize.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.list.model.ListEvent
import com.metoly.morganize.feature.list.model.ListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * ViewModel responsible for feeding data to the main ListScreen.
 * Observes the SQLite database for real-time Note and Category updates,
 * handles filtering by category, and executes basic CRUD operations unassociated with deep editing.
 */
class ListViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var notesJob: Job? = null

    init {
        observeCategories()
        observeNotes()
    }

    fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.DeleteNote -> deleteNote(event)
            is ListEvent.FilterByCategory -> filterByCategory(event.categoryId)
            is ListEvent.CreateCategory -> createCategory(event)
            is ListEvent.SnackbarDismissed -> clearUserMessage()
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

    private fun observeNotes() {
        notesJob?.cancel()
        val categoryId = _uiState.value.selectedCategoryId
        val flow =
            if (categoryId == null) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.getNotesByCategory(categoryId)
            }
        notesJob =
            flow
                .onEach { state -> _uiState.update { it.copy(notesState = state) } }
                .launchIn(viewModelScope)
    }

    private fun filterByCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        observeNotes()
    }

    private fun deleteNote(event: ListEvent.DeleteNote) {
        noteRepository
            .deleteNote(event.note)
            .onEach { state ->
                if (state is ResponseState.Error) {
                    _uiState.update { it.copy(userMessage = state.message) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun createCategory(event: ListEvent.CreateCategory) {
        categoryRepository
            .insertCategory(Category(name = event.name, colorArgb = event.colorArgb))
            .onEach { state ->
                if (state is ResponseState.Error) {
                    _uiState.update { it.copy(userMessage = state.message) }
                }
            }
            .launchIn(viewModelScope)
    }
}
