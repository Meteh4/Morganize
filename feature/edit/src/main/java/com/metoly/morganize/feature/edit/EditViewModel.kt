package com.metoly.morganize.feature.edit

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.RichTextEditorState
import com.metoly.components.applyInlineFormat
import com.metoly.components.continueList
import com.metoly.components.richTextStateFromPersisted
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
                        _uiState.update { it.copy(categories = state.data) }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun onEvent(event: EditEvent) {
        when (event) {
            is EditEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value) }
            }

            is EditEvent.RichTextChanged -> {
                val current = _uiState.value.richTextState
                var updated = current.copy(textFieldValue = event.value)

                if (current.isBoldActive && event.value.text.length > current.text.length) {
                    updated = updated.applyInlineFormat(current, SpanFormatType.BOLD)
                }
                if (current.isItalicActive && event.value.text.length > current.text.length) {
                    updated = updated.applyInlineFormat(current, SpanFormatType.ITALIC)
                }
                if (current.isBulletListActive && event.value.text.length > current.text.length) {
                    val newCursor = event.value.selection.start
                    val oldCursor = current.cursor
                    if (newCursor > oldCursor) {
                        updated = updated.copy(
                            spans = updated.spans + RichSpan(oldCursor, newCursor, SpanFormatType.BULLET_LIST)
                        )
                    }
                }
                if (current.isNumberedListActive && event.value.text.length > current.text.length) {
                    val newCursor = event.value.selection.start
                    val oldCursor = current.cursor
                    if (newCursor > oldCursor) {
                        updated = updated.copy(
                            spans = updated.spans + RichSpan(oldCursor, newCursor, SpanFormatType.NUMBERED_LIST)
                        )
                    }
                }

                _uiState.update { it.copy(richTextState = updated) }
            }

            EditEvent.ToggleBold -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    state.copy(richTextState = rich.copy(isBoldActive = !rich.isBoldActive, isItalicActive = false))
                }
            }

            EditEvent.ToggleItalic -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    state.copy(richTextState = rich.copy(isItalicActive = !rich.isItalicActive, isBoldActive = false))
                }
            }

            EditEvent.ToggleBulletList -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    val newActive = !rich.isBulletListActive
                    var updated = rich.copy(isBulletListActive = newActive, isNumberedListActive = false)
                    if (newActive) {
                        val cursor = rich.cursor
                        val text = rich.text
                        val prefix = "• "
                        val newText = text.substring(0, cursor) + prefix + text.substring(cursor)
                        val newCursor = cursor + prefix.length
                        updated = updated.copy(
                            textFieldValue = updated.textFieldValue.copy(
                                text = newText, selection = TextRange(newCursor)
                            ),
                            spans = updated.spans + RichSpan(cursor, newCursor, SpanFormatType.BULLET_LIST)
                        )
                    }
                    state.copy(richTextState = updated)
                }
            }

            EditEvent.ToggleNumberedList -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    val newActive = !rich.isNumberedListActive
                    var updated = rich.copy(isNumberedListActive = newActive, isBulletListActive = false)
                    if (newActive) {
                        val cursor = rich.cursor
                        val text = rich.text
                        val linesBefore = text.substring(0, cursor).lines()
                        val nextNum = linesBefore.count { it.matches(Regex("^\\d+\\..*")) } + 1
                        val prefix = "$nextNum. "
                        val newText = text.substring(0, cursor) + prefix + text.substring(cursor)
                        val newCursor = cursor + prefix.length
                        updated = updated.copy(
                            textFieldValue = updated.textFieldValue.copy(
                                text = newText, selection = TextRange(newCursor)
                            ),
                            spans = updated.spans + RichSpan(cursor, newCursor, SpanFormatType.NUMBERED_LIST)
                        )
                    }
                    state.copy(richTextState = updated)
                }
            }

            EditEvent.ContinueList -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    val currentLine = getCurrentLine(rich)
                    if (currentLine.trim() == "•" || currentLine.trim().matches(Regex("^\\d+\\.$"))) {
                        state.copy(
                            richTextState = rich.copy(
                                isBulletListActive = false,
                                isNumberedListActive = false
                            )
                        )
                    } else {
                        state.copy(richTextState = rich.continueList())
                    }
                }
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
                                val checklist = try {
                                    if (note.checklistJson.isNotEmpty())
                                        Json.decodeFromString<List<ChecklistItem>>(note.checklistJson)
                                    else emptyList()
                                } catch (_: Exception) { emptyList() }

                                val spans = try {
                                    if (note.richSpansJson.isNotEmpty())
                                        Json.decodeFromString<List<RichSpan>>(note.richSpansJson)
                                    else emptyList()
                                } catch (_: Exception) { emptyList() }

                                _uiState.update {
                                    it.copy(
                                            title = note.title,
                                            richTextState = richTextStateFromPersisted(note.content, spans),
                                            backgroundColor = note.backgroundColor,
                                            categoryId = note.categoryId,
                                            imagePaths = note.imagePaths,
                                            drawingPath = note.drawingPath,
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
        val richState = _uiState.value.richTextState
        val checklistJson = if (_uiState.value.checklistItems.isNotEmpty())
            Json.encodeToString(_uiState.value.checklistItems) else ""

        val updatedNote = current.copy(
                title = _uiState.value.title.trim(),
                content = richState.text.trim(),
                backgroundColor = _uiState.value.backgroundColor,
                categoryId = _uiState.value.categoryId,
                imagePaths = _uiState.value.imagePaths,
                drawingPath = _uiState.value.drawingPath,
                richSpansJson = if (richState.spans.isNotEmpty())
                    Json.encodeToString(richState.spans) else "",
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

    private fun getCurrentLine(state: RichTextEditorState): String {
        val text = state.text
        val cursor = state.cursor
        val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
        return text.substring(lineStart, cursor)
    }
}
