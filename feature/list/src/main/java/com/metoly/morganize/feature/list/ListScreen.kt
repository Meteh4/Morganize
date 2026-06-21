package com.metoly.morganize.feature.list

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.components.AddCategoryBottomSheet
import com.metoly.components.ThemePickerDialog
import com.metoly.morganize.core.data.UserPreferencesRepository
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.ThemeMode
import com.metoly.morganize.feature.list.components.EmptyDetailPane
import com.metoly.morganize.feature.list.components.NoteDetailPane
import com.metoly.morganize.feature.list.components.NoteListPane
import com.metoly.morganize.feature.list.model.ListEvent
import kotlinx.coroutines.launch

/**
 * The primary dashboard screen displaying all user notes.
 * Implements a responsive List/Detail layout via Material 3 Adaptive Scaffold.
 * Supports search, pinning, sorting, grid/list toggle, and undo-delete.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    onCreateNote: () -> Unit,
    onEditNote: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTheme by userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isSinglePane = scaffoldNavigator.scaffoldDirective.maxHorizontalPartitions == 1

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.onEvent(ListEvent.ExportNotes(it)) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onEvent(ListEvent.ImportNotes(it)) }
    }

    // Handle user messages + undo snackbar
    LaunchedEffect(uiState.userMessage, uiState.pendingDeleteNoteId) {
        val message = uiState.userMessage?.asString(context) ?: return@LaunchedEffect
        val pendingDeleteId = uiState.pendingDeleteNoteId

        if (pendingDeleteId != null) {
            // Show undo snackbar for soft delete
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.feature_list_undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(ListEvent.UndoDelete(pendingDeleteId))
            } else {
                viewModel.onEvent(ListEvent.SnackbarDismissed)
            }
        } else {
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ListEvent.SnackbarDismissed)
        }
    }

    BackHandler(
        enabled = isSinglePane && scaffoldNavigator.currentDestination?.contentKey != null
    ) { coroutineScope.launch { scaffoldNavigator.navigateBack() } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)) {
            val notesState = uiState.notesState
            val isLoading = notesState is ResponseState.Loading || notesState is ResponseState.Idle
            val notes = (notesState as? ResponseState.Success)?.data ?: emptyList()
            
            val selectedNoteId = scaffoldNavigator.currentDestination?.contentKey
            val selectedNote = notes.find { it.id == selectedNoteId }

            ListDetailPaneScaffold(
                directive = scaffoldNavigator.scaffoldDirective,
                value = scaffoldNavigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        NoteListPane(
                            notes = notes,
                            categories = uiState.categories,
                            selectedCategoryId = uiState.selectedCategoryId,
                            isLoading = isLoading,
                            searchQuery = uiState.searchQuery,
                            sortOrder = uiState.sortOrder,
                            viewMode = uiState.viewMode,
                            onSearchQueryChanged = { query ->
                                viewModel.onEvent(ListEvent.SearchQueryChanged(query))
                            },
                            onSortOrderChanged = { order ->
                                viewModel.onEvent(ListEvent.SortOrderChanged(order))
                            },
                            onToggleViewMode = {
                                viewModel.onEvent(ListEvent.ToggleViewMode)
                            },
                            onCategorySelected = { categoryId ->
                                viewModel.onEvent(ListEvent.FilterByCategory(categoryId))
                            },
                            selectedNoteId = selectedNoteId,
                            onNoteClick = { note ->
                                coroutineScope.launch {
                                    scaffoldNavigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = note.id
                                    )
                                }
                            },
                            onDeleteNote = { note ->
                                viewModel.onEvent(ListEvent.DeleteNote(note))
                            },
                            onTogglePin = { noteId, isPinned ->
                                viewModel.onEvent(ListEvent.TogglePin(noteId, isPinned))
                            },
                            onCreateNote = onCreateNote,
                            onAddCategory = { showAddCategorySheet = true },
                            onThemeClick = { showThemePicker = true },
                            onExportClick = { exportLauncher.launch("morganize_export.json") },
                            onImportClick = { importLauncher.launch(arrayOf("application/json")) }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        if (selectedNote != null) {
                            NoteDetailPane(
                                note = selectedNote,
                                showBackButton = isSinglePane,
                                onEditClick = { onEditNote(selectedNote.id) },
                                onDuplicateClick = { viewModel.onEvent(ListEvent.DuplicateNote(selectedNote.id)) },
                                onShareClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, selectedNote.toShareText())
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                onBack = {
                                    coroutineScope.launch {
                                        scaffoldNavigator.navigateBack()
                                    }
                                },
                                category = uiState.categories.find { it.id == selectedNote.categoryId }
                            )
                        } else {
                            EmptyDetailPane()
                        }
                    }
                }
            )

            if (notesState is ResponseState.Error) {
                LaunchedEffect(notesState.message) {
                    snackbarHostState.showSnackbar(
                        message = notesState.message
                    )
                }
            }
        }
    }

    if (showAddCategorySheet) {
        AddCategoryBottomSheet(
            onDismiss = { showAddCategorySheet = false },
            onSave = { name, colorArgb ->
                viewModel.onEvent(ListEvent.CreateCategory(name, colorArgb))
            }
        )
    }

    if (showThemePicker) {
        ThemePickerDialog(
            currentTheme = currentTheme,
            onThemeSelected = { mode ->
                coroutineScope.launch {
                    userPreferencesRepository.setThemeMode(mode)
                }
            },
            onDismiss = { showThemePicker = false }
        )
    }
}
