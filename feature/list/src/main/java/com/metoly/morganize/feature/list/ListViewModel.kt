package com.metoly.morganize.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.ExportImportHelper
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.list.model.ListEvent
import com.metoly.morganize.feature.list.model.ListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.net.Uri

/**
 * ViewModel responsible for feeding data to the main ListScreen.
 * Observes the SQLite database for real-time Note and Category updates,
 * handles filtering by category, and executes basic CRUD operations unassociated with deep editing.
 */
class ListViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val exportImportHelper: ExportImportHelper
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
            is ListEvent.DeleteNote -> deleteNote(event.note)
            is ListEvent.FilterByCategory -> filterByCategory(event.categoryId)
            is ListEvent.CreateCategory -> createCategory(event)
            is ListEvent.SnackbarDismissed -> clearUserMessage()
            is ListEvent.ExportNotes -> exportNotes(event.uri)
            is ListEvent.ImportNotes -> importNotes(event.uri)
            is ListEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is ListEvent.TogglePin -> togglePin(event.noteId, event.isPinned)
            is ListEvent.SortOrderChanged -> updateSortOrder(event.order)
            is ListEvent.ToggleViewMode -> toggleViewMode()
            is ListEvent.UndoDelete -> undoDelete(event.noteId)
            is ListEvent.DuplicateNote -> duplicateNote(event.noteId)
        }
    }

    private fun exportNotes(uri: Uri) {
        viewModelScope.launch {
            val result = exportImportHelper.exportNotes(uri)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_export_success)) }
            } else {
                _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_export_failed)) }
            }
        }
    }

    private fun importNotes(uri: Uri) {
        viewModelScope.launch {
            val result = exportImportHelper.importNotes(uri)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_import_success, result.getOrNull() ?: 0)) }
            } else {
                _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_import_failed)) }
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

    private fun observeNotes() {
        notesJob?.cancel()
        val state = _uiState.value
        val query = state.searchQuery.trim()

        val flow = when {
            query.isNotEmpty() -> noteRepository.searchNotes(query)
            state.selectedCategoryId != null -> noteRepository.getNotesByCategory(state.selectedCategoryId)
            else -> noteRepository.getAllNotes()
        }

        notesJob = flow
            .onEach { responseState ->
                val sorted = if (responseState is ResponseState.Success) {
                    @Suppress("UNCHECKED_CAST")
                    ResponseState.Success(applySorting(responseState.data as List<Note>, state.sortOrder))
                } else {
                    responseState
                }
                _uiState.update { it.copy(notesState = sorted as ResponseState<List<Note>>) }
            }
            .launchIn(viewModelScope)
    }

    private fun filterByCategory(categoryId: Long?) {
        _uiState.update { it.copy(userMessage = null, pendingDeleteNoteId = null) }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            // Soft delete
            val deletedNote = note.copy(isDeleted = true, deletedAt = System.currentTimeMillis())
            noteRepository.updateNote(deletedNote)
            _uiState.update {
                it.copy(
                    userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_note_deleted),
                    pendingDeleteNoteId = note.id
                )
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        observeNotes()
    }

    private fun togglePin(noteId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            noteRepository.setPinned(noteId, isPinned)
        }
    }

    private fun updateSortOrder(order: com.metoly.morganize.feature.list.model.NoteSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        observeNotes()
    }

    private fun toggleViewMode() {
        _uiState.update {
            val newMode = if (it.viewMode == com.metoly.morganize.feature.list.model.NoteViewMode.LIST) 
                com.metoly.morganize.feature.list.model.NoteViewMode.GRID 
            else 
                com.metoly.morganize.feature.list.model.NoteViewMode.LIST
            it.copy(viewMode = newMode)
        }
    }

    private fun undoDelete(noteId: Long) {
        viewModelScope.launch {
            val state = noteRepository.getNoteById(noteId).firstOrNull()
            val note = (state as? ResponseState.Success<Note>)?.data ?: return@launch
            val restored = note.copy(isDeleted = false, deletedAt = null)
            noteRepository.updateNote(restored)
            _uiState.update { it.copy(userMessage = null, pendingDeleteNoteId = null) }
        }
    }

    private fun duplicateNote(noteId: Long) {
        viewModelScope.launch {
            val state = noteRepository.getNoteById(noteId).firstOrNull()
            val note = (state as? ResponseState.Success<Note>)?.data ?: return@launch
            val duplicated = note.copy(
                id = 0,
                title = note.title + " (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteRepository.insertNote(duplicated)
            _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.StringResource(R.string.feature_list_note_duplicated)) }
        }
    }

    private fun applySorting(notes: List<Note>, order: com.metoly.morganize.feature.list.model.NoteSortOrder): List<Note> {
        val pinned = notes.filter { it.isPinned }
        val unpinned = notes.filter { !it.isPinned }
        val sortedPinned = sortByOrder(pinned, order)
        val sortedUnpinned = sortByOrder(unpinned, order)
        return sortedPinned + sortedUnpinned
    }

    private fun sortByOrder(notes: List<Note>, order: com.metoly.morganize.feature.list.model.NoteSortOrder): List<Note> = when (order) {
        com.metoly.morganize.feature.list.model.NoteSortOrder.UPDATED_DESC -> notes.sortedByDescending { it.updatedAt }
        com.metoly.morganize.feature.list.model.NoteSortOrder.UPDATED_ASC -> notes.sortedBy { it.updatedAt }
        com.metoly.morganize.feature.list.model.NoteSortOrder.TITLE_ASC -> notes.sortedBy { it.title.lowercase() }
        com.metoly.morganize.feature.list.model.NoteSortOrder.TITLE_DESC -> notes.sortedByDescending { it.title.lowercase() }
        com.metoly.morganize.feature.list.model.NoteSortOrder.CREATED_DESC -> notes.sortedByDescending { it.createdAt }
    }

    private fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun createCategory(event: ListEvent.CreateCategory) {
        categoryRepository
            .insertCategory(Category(name = event.name, colorArgb = event.colorArgb))
            .onEach { state ->
                if (state is ResponseState.Error) {
                    _uiState.update { it.copy(userMessage = com.metoly.morganize.core.ui.UiText.DynamicString(state.message)) }
                }
            }
            .launchIn(viewModelScope)
    }
}
