package com.metoly.morganize.feature.create

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.components.AddItemBottomSheet
import com.metoly.components.NoteEditorBottomBar
import com.metoly.components.NoteEditorContent
import com.metoly.components.RichTextEditorState
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.components.rememberNoteImagePicker
import com.metoly.morganize.feature.create.components.CreateTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateScreen(viewModel: CreateViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    var activeRichState by remember { mutableStateOf<RichTextEditorState?>(null) }
    var activeEditingTextItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.editingTextItemId) {
        activeEditingTextItemId = uiState.editingTextItemId
        if (uiState.editingTextItemId == null) {
            activeRichState = null
        } else {
            activeRichState = uiState.editingRichState
        }
    }

    val updateRichState: (RichTextEditorState) -> Unit = { newState ->
        activeRichState = newState
        viewModel.delegate.onEvent(NoteEditorEvent.RichStateUpdated(newState))
    }

    var showAddItemSheet by remember { mutableStateOf(false) }
    var isPending5x5Image by remember { mutableStateOf(false) }
    var activePageIndex by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberNoteImagePicker { path ->
        if (isPending5x5Image) {
            viewModel.delegate.onEvent(NoteEditorEvent.ImageGridItemAdded(path, targetPageIndex = activePageIndex, width = 5, height = 5))
            isPending5x5Image = false
        } else {
            viewModel.delegate.onEvent(NoteEditorEvent.ImageGridItemAdded(path = path, targetPageIndex = activePageIndex))
        }
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoteEditorUiEvent.SaveSuccess -> onSaved()
                is NoteEditorUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is NoteEditorUiEvent.ScrollToPage -> lazyListState.animateScrollToItem(event.pageIndex + 1)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        topBar = {
            CreateTopBar(
                onBack = onBack,
                selectedColor = uiState.backgroundColor,
                onColorSelected = { colorArgb ->
                    viewModel.delegate.onEvent(NoteEditorEvent.BackgroundColorChanged(colorArgb = colorArgb))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NoteEditorBottomBar(
                state = uiState,
                activePageIndex = activePageIndex,
                activeEditingTextItemId = activeEditingTextItemId,
                activeRichState = activeRichState,
                onRichStateUpdate = updateRichState,
                onEvent = viewModel.delegate::onEvent,
                onSave = viewModel::save,
                saveContentDescription = stringResource(R.string.feature_create_save),
                imagePickerLauncher = imagePickerLauncher
            )
        }
    ) { padding ->
        NoteEditorContent(
            state = uiState,
            onEvent = viewModel.delegate::onEvent,
            titleHint = stringResource(R.string.feature_create_title_hint),
            activeEditingTextItemId = activeEditingTextItemId,
            activeRichState = activeRichState,
            onActiveRichStateChange = updateRichState,
            onEmptyGridAddClicked = { showAddItemSheet = true },
            onActivePageChanged = { activePageIndex = it },
            lazyListState = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }

    if (showAddItemSheet) {
        AddItemBottomSheet(
            onDismiss = { showAddItemSheet = false },
            onAddText = {
                viewModel.delegate.onEvent(NoteEditorEvent.TextGridItemAdded("", activePageIndex, width = 5, height = 5))
            },
            onAddImage = { isPending5x5Image = true },
            onAddChecklist = {
                viewModel.delegate.onEvent(NoteEditorEvent.ChecklistGridItemAdded(activePageIndex, width = 5, height = 5))
            },
            imagePickerLauncher = imagePickerLauncher
        )
    }
}