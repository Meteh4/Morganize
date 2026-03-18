package com.metoly.morganize.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.RichTextEditorState
import com.metoly.components.applyInlineFormat
import com.metoly.components.continueList
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType
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

            is CreateEvent.RichTextChanged -> {
                val current = _uiState.value.richTextState
                var updated = current.copy(textFieldValue = event.value)

                // Extend active inline format spans as the user types
                if (current.isBoldActive && event.value.text.length > current.text.length) {
                    updated = updated.applyInlineFormat(current, SpanFormatType.BOLD)
                }
                if (current.isItalicActive && event.value.text.length > current.text.length) {
                    updated = updated.applyInlineFormat(current, SpanFormatType.ITALIC)
                }

                // Handle bullet list prefix auto-insertion on entering list mode
                if (current.isBulletListActive && event.value.text.length > current.text.length) {
                    // Mark bullet prefix range
                    val newCursor = event.value.selection.start
                    val oldCursor = current.cursor
                    if (newCursor > oldCursor) {
                        updated = updated.copy(
                            spans = updated.spans + RichSpan(
                                start = oldCursor,
                                end = newCursor,
                                type = SpanFormatType.BULLET_LIST
                            )
                        )
                    }
                }

                if (current.isNumberedListActive && event.value.text.length > current.text.length) {
                    val newCursor = event.value.selection.start
                    val oldCursor = current.cursor
                    if (newCursor > oldCursor) {
                        updated = updated.copy(
                            spans = updated.spans + RichSpan(
                                start = oldCursor,
                                end = newCursor,
                                type = SpanFormatType.NUMBERED_LIST
                            )
                        )
                    }
                }

                _uiState.update { it.copy(richTextState = updated) }
            }

            CreateEvent.ToggleBold -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    state.copy(
                        richTextState = rich.copy(
                            isBoldActive = !rich.isBoldActive,
                            isItalicActive = false // mutually exclusive inline formats
                        )
                    )
                }
            }

            CreateEvent.ToggleItalic -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    state.copy(
                        richTextState = rich.copy(
                            isItalicActive = !rich.isItalicActive,
                            isBoldActive = false
                        )
                    )
                }
            }

            CreateEvent.ToggleBulletList -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    val newActive = !rich.isBulletListActive
                    var updated = rich.copy(
                        isBulletListActive = newActive,
                        isNumberedListActive = false
                    )
                    // Insert initial bullet prefix if turning on
                    if (newActive) {
                        val cursor = rich.cursor
                        val text = rich.text
                        val prefix = "• "
                        val newText = text.substring(0, cursor) + prefix + text.substring(cursor)
                        val newCursor = cursor + prefix.length
                        updated = updated.copy(
                            textFieldValue = updated.textFieldValue.copy(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursor)
                            ),
                            spans = updated.spans + RichSpan(
                                start = cursor, end = newCursor, type = SpanFormatType.BULLET_LIST
                            )
                        )
                    }
                    state.copy(richTextState = updated)
                }
            }

            CreateEvent.ToggleNumberedList -> {
                _uiState.update { state ->
                    val rich = state.richTextState
                    val newActive = !rich.isNumberedListActive
                    var updated = rich.copy(
                        isNumberedListActive = newActive,
                        isBulletListActive = false
                    )
                    // Insert initial number prefix if turning on
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
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursor)
                            ),
                            spans = updated.spans + RichSpan(
                                start = cursor, end = newCursor, type = SpanFormatType.NUMBERED_LIST
                            )
                        )
                    }
                    state.copy(richTextState = updated)
                }
            }

            CreateEvent.ContinueList -> {
                _uiState.update { state ->
                    val currentLine = getCurrentLine(state.richTextState)
                    // If current line is just a bullet/number with no content → exit list mode
                    if (currentLine.trim() == "•" || currentLine.trim().matches(Regex("^\\d+\\.$"))) {
                        state.copy(
                            richTextState = state.richTextState.copy(
                                isBulletListActive = false,
                                isNumberedListActive = false
                            )
                        )
                    } else {
                        state.copy(richTextState = state.richTextState.continueList())
                    }
                }
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
        val plainText = currentState.richTextState.text.trim()

        if (currentState.title.isBlank() &&
            plainText.isBlank() &&
            currentState.imagePaths.isEmpty() &&
            currentState.drawingPath == null &&
            !hasChecklist
        ) return

        val note = Note(
            title = currentState.title.trim(),
            content = plainText,
            backgroundColor = currentState.backgroundColor,
            categoryId = currentState.categoryId,
            imagePaths = currentState.imagePaths,
            drawingPath = currentState.drawingPath,
            richSpansJson = if (currentState.richTextState.spans.isNotEmpty())
                Json.encodeToString(currentState.richTextState.spans)
            else "",
            checklistJson = if (hasChecklist)
                Json.encodeToString(currentState.checklistItems)
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

    private fun getCurrentLine(state: RichTextEditorState): String {
        val text = state.text
        val cursor = state.cursor
        val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
        return text.substring(lineStart, cursor)
    }
}
