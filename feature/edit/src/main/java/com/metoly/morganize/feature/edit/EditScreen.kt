package com.metoly.morganize.feature.edit

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.graphics.Color
import com.metoly.components.DeleteNoteDialog
import com.metoly.components.RichTextEditorState
import com.metoly.components.rememberNoteImagePicker
import com.metoly.morganize.feature.edit.components.EditBottomBar
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

    var showAddItemSheet by remember { mutableStateOf(false) }
    var isPending5x5Image by remember { mutableStateOf(false) }
    var activePageIndex by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberNoteImagePicker { path ->
        if (isPending5x5Image) {
            viewModel.onEvent(EditEvent.ImageGridItemAdded(path, targetPageIndex = activePageIndex, width = 5, height = 5))
            isPending5x5Image = false
        } else {
            viewModel.onEvent(EditEvent.ImageGridItemAdded(path = path, targetPageIndex = activePageIndex))
        }
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

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
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
            EditBottomBar(
                pages = uiState.pages,
                activePageIndex = activePageIndex,
                isDrawingMode = uiState.isDrawingMode,
                isEraserMode = uiState.isEraserMode,
                drawingPenColorArgb = uiState.drawingPenColorArgb,
                drawingStrokeWidthFraction = uiState.drawingStrokeWidthFraction,
                drawingEraserWidthFraction = uiState.drawingEraserWidthFraction,
                activeEditingTextItemId = activeEditingTextItemId,
                activeRichState = activeRichState,
                onRichStateUpdate = updateRichState,
                onEvent = viewModel::onEvent,
                imagePickerLauncher = imagePickerLauncher
            )
        }
    ) { padding ->
        EditNoteContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            activeEditingTextItemId = activeEditingTextItemId,
            activeRichState = activeRichState,
            onActiveRichStateChange = updateRichState,
            onEmptyGridAddClicked = { showAddItemSheet = true },
            onActivePageChanged = { activePageIndex = it },
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
                            viewModel.onEvent(EditEvent.TextGridItemAdded("", activePageIndex, width = 5, height = 5))
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
                            viewModel.onEvent(EditEvent.ChecklistGridItemAdded(activePageIndex, width = 5, height = 5))
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