package com.metoly.morganize.feature.create

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
import androidx.compose.ui.Alignment
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
import com.metoly.components.RichTextEditorState
import com.metoly.components.RichTextToolbar
import com.metoly.components.NoteBottomBar
import com.metoly.components.clampedFontSize
import com.metoly.components.nextLineHeight
import com.metoly.components.nextTextAlign
import com.metoly.components.rememberNoteImagePicker
import com.metoly.components.toggleFormat
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.feature.create.components.CreateNoteContent
import com.metoly.morganize.feature.create.components.CreateTopBar
import com.metoly.morganize.feature.create.model.CreateEvent

@Composable
fun CreateScreen(viewModel: CreateViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
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
        viewModel.onEvent(CreateEvent.RichStateUpdated(newState))
    }

    var isPending5x5Image by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberNoteImagePicker {
        if (isPending5x5Image) {
            viewModel.onEvent(CreateEvent.ImageGridItemAdded(it, width = 5, height = 5))
            isPending5x5Image = false
        } else {
            viewModel.onEvent(CreateEvent.ImageGridItemAdded(it))
        }
    }

    var showAddItemSheet by remember { mutableStateOf(false) }

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
                // Rich text toolbar – slides in above the bottom bar when text editing is active
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
                    val canUndoDrawing = uiState.pages.any { it.drawingData.isNotBlank() }
                    
                    com.metoly.components.DrawingToolbar(
                        penColorArgb = uiState.drawingPenColorArgb,
                        strokeWidthFraction = uiState.drawingStrokeWidthFraction,
                        eraserWidthFraction = uiState.drawingEraserWidthFraction,
                        isEraserMode = uiState.isEraserMode,
                        canUndo = canUndoDrawing,
                        onColorSelected = { viewModel.onEvent(CreateEvent.DrawingColorChanged(it)) },
                        onStrokeWidthChange = { viewModel.onEvent(CreateEvent.DrawingStrokeWidthChanged(it)) },
                        onEraserWidthChange = { viewModel.onEvent(CreateEvent.DrawingEraserWidthChanged(it)) },
                        onToggleEraser = { viewModel.onEvent(CreateEvent.DrawingEraserToggled) },
                        onUndo = {
                            uiState.pages
                                .filter { it.drawingData.isNotBlank() }
                                .forEach { page ->
                                    viewModel.onEvent(CreateEvent.DrawingStrokeReverted(page.id))
                                }
                        },
                        onClose = { viewModel.onEvent(CreateEvent.DrawingModeToggled) }
                    )
                }

                NoteBottomBar(
                    onAddText = { viewModel.onEvent(CreateEvent.TextGridItemAdded("")) },
                    onAddImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddChecklist = { viewModel.onEvent(CreateEvent.ChecklistGridItemAdded()) },
                    onStartDrawing = { viewModel.onEvent(CreateEvent.DrawingModeToggled) },
                    onSave = { viewModel.onEvent(CreateEvent.Save) },
                    saveContentDescription = stringResource(R.string.feature_create_save)
                )
            }
        }
    ) { padding ->
        CreateNoteContent(
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
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showAddItemSheet = false },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "Add Item",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val bottomSheetButtonStyle = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                
                Row(
                    modifier = bottomSheetButtonStyle
                        .clickable {
                            viewModel.onEvent(CreateEvent.TextGridItemAdded("", width = 5, height = 5))
                            showAddItemSheet = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(Icons.Default.TextFields, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    androidx.compose.material3.Text("Text", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
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
                    androidx.compose.material3.Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    androidx.compose.material3.Text("Image", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = bottomSheetButtonStyle
                        .clickable {
                            viewModel.onEvent(CreateEvent.ChecklistGridItemAdded(width = 5, height = 5))
                            showAddItemSheet = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(Icons.Default.Checklist, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    androidx.compose.material3.Text("Checklist", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}