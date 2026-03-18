package com.metoly.morganize.feature.create

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import com.metoly.components.NoteBottomBar
import com.metoly.components.NoteDrawingDialog
import com.metoly.components.RichTextToolbar
import com.metoly.components.rememberNoteImagePicker
import com.metoly.morganize.feature.create.components.CreateNoteContent
import com.metoly.morganize.feature.create.components.CreateTopBar
import com.metoly.morganize.feature.create.model.CreateEvent

@Composable
fun CreateScreen(viewModel: CreateViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showRichTextTools by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberNoteImagePicker {
        viewModel.onEvent(CreateEvent.ImageAdded(it))
    }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) {
            onSaved()
            viewModel.onEvent(CreateEvent.NavigationHandled)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateEvent.SnackbarDismissed)
        }
    }

    if (showDrawingDialog) {
        NoteDrawingDialog(
            onDismiss = { showDrawingDialog = false },
            onDrawingSaved = {
                viewModel.onEvent(CreateEvent.DrawingChanged(it))
                showDrawingDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CreateTopBar(
                onBack = onBack,
                selectedColor = uiState.backgroundColor,
                onColorSelected = { viewModel.onEvent(CreateEvent.BackgroundColorChanged(it)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier
                .imePadding()
                .fillMaxWidth()) {
                AnimatedVisibility(visible = showRichTextTools) {
                    RichTextToolbar(
                        isBoldActive = uiState.richTextState.isBoldActive,
                        isItalicActive = uiState.richTextState.isItalicActive,
                        isBulletListActive = uiState.richTextState.isBulletListActive,
                        isNumberedListActive = uiState.richTextState.isNumberedListActive,
                        onToggleBold = { viewModel.onEvent(CreateEvent.ToggleBold) },
                        onToggleItalic = { viewModel.onEvent(CreateEvent.ToggleItalic) },
                        onToggleBulletList = { viewModel.onEvent(CreateEvent.ToggleBulletList) },
                        onToggleNumberedList = { viewModel.onEvent(CreateEvent.ToggleNumberedList) }
                    )
                }
                NoteBottomBar(
                    onAddImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDraw = { showDrawingDialog = true },
                    onAddChecklistItem = { viewModel.onEvent(CreateEvent.ChecklistItemAdded("")) },
                    onToggleRichTextTools = { showRichTextTools = !showRichTextTools },
                    onSave = { viewModel.onEvent(CreateEvent.Save) },
                    saveContentDescription = stringResource(R.string.feature_create_save)
                )
            }
        }
    ) { padding ->
        CreateNoteContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding()
        )
    }
}