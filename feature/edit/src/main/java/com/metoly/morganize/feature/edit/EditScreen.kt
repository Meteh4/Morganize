package com.metoly.morganize.feature.edit

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.metoly.components.DrawingToolbar
import com.metoly.components.NoteBottomBar
import com.metoly.components.RichTextEditorState
import com.metoly.components.RichTextToolbar
import com.metoly.components.clampedFontSize
import com.metoly.components.nextLineHeight
import com.metoly.components.nextTextAlign
import com.metoly.components.rememberNoteImagePicker
import com.metoly.components.toggleFormat
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.feature.edit.components.EditNoteContent
import com.metoly.morganize.feature.edit.components.EditTopBar
import com.metoly.morganize.feature.edit.model.EditEvent

@Composable
fun EditScreen(viewModel: EditViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        viewModel.onEvent(EditEvent.RichStateUpdated(newState))
    }

    var isPending5x5Image by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberNoteImagePicker {
        if (isPending5x5Image) {
            viewModel.onEvent(EditEvent.ImageGridItemAdded(it, width = 5, height = 5))
            isPending5x5Image = false
        } else {
            viewModel.onEvent(EditEvent.ImageGridItemAdded(it))
        }
    }

    var showAddItemSheet by remember { mutableStateOf(false) }

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

    // Determine the active page ID for undo — use the first page as default
    uiState.pages.firstOrNull()?.id ?: ""
    val canUndoDrawing = uiState.isDrawingMode &&
        uiState.pages.any { it.drawingData.isNotBlank() }

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
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
            ) {
                // ── Rich text toolbar (shown when a text item is being edited) ──
                AnimatedVisibility(
                    visible = activeEditingTextItemId != null && activeRichState != null && !uiState.isDrawingMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    activeRichState?.let { richState ->
                        RichTextToolbar(
                            state = richState,
                            onToggleBold = { updateRichState(richState.toggleFormat(SpanFormatType.BOLD)) },
                            onToggleItalic = { updateRichState(richState.toggleFormat(SpanFormatType.ITALIC)) },
                            onToggleBulletList = { updateRichState(richState.toggleFormat(SpanFormatType.BULLET_LIST)) },
                            onToggleNumberedList = { updateRichState(richState.toggleFormat(SpanFormatType.NUMBERED_LIST)) },
                            onFontSizeIncrease = { updateRichState(richState.copy(fontSize = clampedFontSize(richState.fontSize, 2f))) },
                            onFontSizeDecrease = { updateRichState(richState.copy(fontSize = clampedFontSize(richState.fontSize, -2f))) },
                            onTextAlignCycle = { updateRichState(richState.copy(textAlign = nextTextAlign(richState.textAlign))) },
                            onLineHeightCycle = { updateRichState(richState.copy(lineHeight = nextLineHeight(richState.lineHeight))) }
                        )
                    }
                }

                // ── Drawing toolbar (shown when drawing mode is active) ────────
                AnimatedVisibility(
                    visible = uiState.isDrawingMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    DrawingToolbar(
                        penColorArgb = uiState.drawingPenColorArgb,
                        strokeWidthFraction = uiState.drawingStrokeWidthFraction,
                        eraserWidthFraction = uiState.drawingEraserWidthFraction,
                        isEraserMode = uiState.isEraserMode,
                        canUndo = canUndoDrawing,
                        onColorSelected = { viewModel.onEvent(EditEvent.DrawingColorChanged(it)) },
                        onStrokeWidthChange = { viewModel.onEvent(EditEvent.DrawingStrokeWidthChanged(it)) },
                        onEraserWidthChange = { viewModel.onEvent(EditEvent.DrawingEraserWidthChanged(it)) },
                        onToggleEraser = { viewModel.onEvent(EditEvent.DrawingEraserToggled) },
                        onUndo = {
                            // Revert the last stroke on every page that has drawing data
                            uiState.pages
                                .filter { it.drawingData.isNotBlank() }
                                .forEach { page ->
                                    viewModel.onEvent(EditEvent.DrawingStrokeReverted(page.id))
                                }
                        },
                        onClose = { viewModel.onEvent(EditEvent.DrawingModeToggled) }
                    )
                }

                // ── Bottom action bar ─────────────────────────────────────────
                NoteBottomBar(
                    onAddText = { viewModel.onEvent(EditEvent.TextGridItemAdded("")) },
                    onAddImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddChecklist = { viewModel.onEvent(EditEvent.ChecklistGridItemAdded()) },
                    onStartDrawing = { viewModel.onEvent(EditEvent.DrawingModeToggled) },
                    onSave = { viewModel.onEvent(EditEvent.Save) },
                    saveContentDescription = stringResource(R.string.feature_edit_save)
                )
            }
        }
    ) { padding ->
        EditNoteContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            activeEditingTextItemId = activeEditingTextItemId,
            activeRichState = activeRichState,
            onActiveRichStateChange = updateRichState,
            onEmptyGridAddClicked = { showAddItemSheet = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }

    if (showAddItemSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showAddItemSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Item",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val bottomSheetButtonStyle = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                
                Row(
                    modifier = bottomSheetButtonStyle
                        .clickable {
                            viewModel.onEvent(EditEvent.TextGridItemAdded("", width = 5, height = 5))
                            showAddItemSheet = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text("Text", style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = bottomSheetButtonStyle
                        .clickable {
                            isPending5x5Image = true
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            showAddItemSheet = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text("Image", style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = bottomSheetButtonStyle
                        .clickable {
                            viewModel.onEvent(EditEvent.ChecklistGridItemAdded(width = 5, height = 5))
                            showAddItemSheet = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Checklist, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text("Checklist", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}