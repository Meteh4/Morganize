package com.metoly.morganize.feature.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.components.DeleteNoteDialog
import com.metoly.components.DrawingCanvas
import com.metoly.morganize.feature.edit.components.EditNoteContent
import com.metoly.morganize.feature.edit.components.EditTopBar
import com.metoly.morganize.feature.edit.model.EditEvent

@Composable
fun EditScreen(viewModel: EditViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDrawingDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) { }
                    viewModel.onEvent(EditEvent.ImageAdded(it.toString()))
                }
            }
        )

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) {
            onDone()
            viewModel.onEvent(EditEvent.NavigationHandled)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
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
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) { Icon(Icons.Default.Image, contentDescription = "Add Image") }

                    IconButton(onClick = { showDrawingDialog = true }) {
                        Icon(Icons.Default.Brush, contentDescription = "Draw")
                    }

                    IconButton(
                        onClick = { viewModel.onEvent(EditEvent.MarkdownToggled) }
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Toggle Markdown")
                    }

                    IconButton(
                        onClick = { viewModel.onEvent(EditEvent.ChecklistItemAdded("")) }
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = "Add Checklist Item")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(EditEvent.Save) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.feature_edit_save),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
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

        if (showDrawingDialog) {
            Dialog(onDismissRequest = { showDrawingDialog = false }) {
                Box(
                    modifier = Modifier
                        .height(400.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    DrawingCanvas(
                        modifier = Modifier.fillMaxSize(),
                        onSave = { bitmap ->
                            val path = saveBitmapToInternalStorage(context, bitmap)
                            viewModel.onEvent(EditEvent.DrawingChanged(path))
                            showDrawingDialog = false
                        }
                    )
                }
            }
        }
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val fileName = "drawing_${System.currentTimeMillis()}.png"
    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return context.getFileStreamPath(fileName).absolutePath
}