package com.metoly.morganize.feature.edit

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
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
import com.metoly.components.DeleteNoteDialog
import com.metoly.components.MarkdownToolbar
import com.metoly.components.NoteBottomBar
import com.metoly.components.NoteDrawingDialog
import com.metoly.components.rememberNoteImagePicker
import com.metoly.morganize.feature.edit.components.EditNoteContent
import com.metoly.morganize.feature.edit.components.EditTopBar
import com.metoly.morganize.feature.edit.model.EditEvent

@Composable
fun EditScreen(viewModel: EditViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDrawingDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberNoteImagePicker {
        viewModel.onEvent(EditEvent.ImageAdded(it))
    }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) {
            onDone()
            viewModel.onEvent(EditEvent.NavigationHandled)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditEvent.SnackbarDismissed)
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteNoteDialog(
            onConfirm = { viewModel.onEvent(EditEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(EditEvent.DeleteDismissed) }
        )
    }

    if (showDrawingDialog) {
        NoteDrawingDialog(
            onDismiss = { showDrawingDialog = false },
            onDrawingSaved = {
                viewModel.onEvent(EditEvent.DrawingChanged(it))
                showDrawingDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            EditTopBar(
                onBack = onBack,
                onDeleteClick = { viewModel.onEvent(EditEvent.DeleteRequested) },
                selectedColor = uiState.backgroundColor,
                onColorSelected = { viewModel.onEvent(EditEvent.BackgroundColorChanged(it)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = uiState.isMarkdownEnabled,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MarkdownToolbar(
                        onBold = {
                            viewModel.onEvent(EditEvent.ContentChanged(uiState.content + "**text**"))
                        },
                        onItalic = {
                            viewModel.onEvent(EditEvent.ContentChanged(uiState.content + "*text*"))
                        },
                        onHeading = {
                            viewModel.onEvent(EditEvent.ContentChanged(uiState.content + "\n# text"))
                        },
                        onBulletList = {
                            viewModel.onEvent(EditEvent.ContentChanged(uiState.content + "\n- text"))
                        },
                        onNumberedList = {
                            viewModel.onEvent(EditEvent.ContentChanged(uiState.content + "\n1. text"))
                        }
                    )
                }
                NoteBottomBar(
                    onAddImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDraw = { showDrawingDialog = true },
                    onToggleMarkdown = { viewModel.onEvent(EditEvent.MarkdownToggled) },
                    onAddChecklistItem = { viewModel.onEvent(EditEvent.ChecklistItemAdded("")) },
                    onSave = { viewModel.onEvent(EditEvent.Save) },
                    saveContentDescription = stringResource(R.string.feature_edit_save)
                )
            }
        }
    ) { padding ->
        EditNoteContent(
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