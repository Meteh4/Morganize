package com.metoly.morganize.feature.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.list.components.EmptyDetailPane
import com.metoly.morganize.feature.list.components.NoteDetailPane
import com.metoly.morganize.feature.list.components.NoteListPane
import com.metoly.morganize.feature.list.model.ListEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(viewModel: ListViewModel, onCreateNote: () -> Unit, onEditNote: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isSinglePane = scaffoldNavigator.scaffoldDirective.maxHorizontalPartitions == 1

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
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
            when (val notesState = uiState.notesState) {
                ResponseState.Idle, ResponseState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ResponseState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = notesState.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is ResponseState.Success -> {
                    val notes = notesState.data
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
                                    onCategorySelected = { categoryId ->
                                        viewModel.onEvent(
                                            ListEvent.FilterByCategory(categoryId)
                                        )
                                    },
                                    selectedNoteId = selectedNoteId,
                                    onNoteClick = { note ->
                                        coroutineScope.launch {
                                            scaffoldNavigator.navigateTo(
                                                pane =
                                                    ListDetailPaneScaffoldRole
                                                        .Detail,
                                                contentKey = note.id
                                            )
                                        }
                                    },
                                    onDeleteNote = { note ->
                                        viewModel.onEvent(ListEvent.DeleteNote(note))
                                    },
                                    onCreateNote = onCreateNote
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
                }
            }
        }
    }
}
