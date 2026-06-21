package com.metoly.morganize.feature.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metoly.components.CategoryChipRow
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R
import com.metoly.morganize.core.ui.R as CoreUiR
import com.metoly.morganize.feature.list.model.NoteSortOrder
import com.metoly.morganize.feature.list.model.NoteViewMode

/**
 * The primary master screen portion of the dual-pane List layout.
 * Hosts search, category filters, sort/view controls, note creation fab,
 * and the actual note list (list or grid mode).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteListPane(
    modifier: Modifier = Modifier,
    notes: List<Note>,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    selectedNoteId: Long?,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onCreateNote: () -> Unit,
    onAddCategory: (() -> Unit)? = null,
    isLoading: Boolean = false,
    searchQuery: String = "",
    sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC,
    viewMode: NoteViewMode = NoteViewMode.LIST,
    onSearchQueryChanged: (String) -> Unit = {},
    onSortOrderChanged: (NoteSortOrder) -> Unit = {},
    onToggleViewMode: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    var isSearchActive by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = onSearchQueryChanged,
                                    onSearch = { /* search is live */ },
                                    expanded = false,
                                    onExpandedChange = {},
                                    placeholder = { Text(stringResource(R.string.feature_list_search_placeholder)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.feature_list_search))
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            onSearchQueryChanged("")
                                            isSearchActive = false
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.feature_list_close_search))
                                        }
                                    }
                                )
                            },
                            expanded = false,
                            onExpandedChange = {},
                            content = {}
                        )
                    } else {
                        Text(
                            stringResource(R.string.feature_list_app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        // Search button
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.feature_list_search))
                        }
                    }

                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Outlined.SortByAlpha, contentDescription = stringResource(R.string.feature_list_sort))
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            currentOrder = sortOrder,
                            onOrderSelected = { order ->
                                onSortOrderChanged(order)
                                showSortMenu = false
                            },
                            onDismiss = { showSortMenu = false }
                        )
                    }

                    // Grid/List toggle
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (viewMode == NoteViewMode.LIST)
                                Icons.Outlined.GridView
                            else
                                Icons.AutoMirrored.Outlined.ViewList,
                            contentDescription = if (viewMode == NoteViewMode.LIST) stringResource(R.string.feature_list_switch_to_grid) else stringResource(R.string.feature_list_switch_to_list)
                        )
                    }

                    // More Options button
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.feature_list_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feature_list_theme)) },
                                onClick = {
                                    showOptionsMenu = false
                                    onThemeClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feature_list_export_notes)) },
                                onClick = {
                                    showOptionsMenu = false
                                    onExportClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feature_list_import_notes)) },
                                onClick = {
                                    showOptionsMenu = false
                                    onImportClick()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(
                    painterResource(id = CoreUiR.drawable.add),
                    contentDescription = stringResource(R.string.feature_list_create_note)
                )
            }
        }
    ) { padding ->
        if (notes.isEmpty() && categories.isEmpty() && !isLoading && searchQuery.isEmpty()) {
            EmptyListContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (categories.isNotEmpty()) {
                    CategoryChipRow(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                        onAddCategory = onAddCategory,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (isLoading && notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (notes.isEmpty()) {
                    EmptyListContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    when (viewMode) {
                        NoteViewMode.LIST -> {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(notes, key = { it.id }) { note ->
                                    val category = categories.find { it.id == note.categoryId }
                                    SwipeToDismissNoteCard(
                                        note = note,
                                        category = category,
                                        isSelected = note.id == selectedNoteId,
                                        onClick = { onNoteClick(note) },
                                        onDeleteConfirmed = { onDeleteNote(note) },
                                        onPinToggle = { onTogglePin(note.id, note.isPinned) },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                            fadeOutSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    )
                                }
                            }
                        }
                        NoteViewMode.GRID -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(notes, key = { it.id }) { note ->
                                    val category = categories.find { it.id == note.categoryId }
                                    NoteCard(
                                        note = note,
                                        category = category,
                                        isSelected = note.id == selectedNoteId,
                                        onClick = { onNoteClick(note) },
                                        onPinToggle = { onTogglePin(note.id, note.isPinned) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dropdown menu for selecting the note sort order.
 */
@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentOrder: NoteSortOrder,
    onOrderSelected: (NoteSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        NoteSortOrder.entries.forEach { order ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = order == currentOrder,
                            onClick = null
                        )
                        Text(
                            text = order.displayName(),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                onClick = { onOrderSelected(order) }
            )
        }
    }
}

private fun NoteSortOrder.displayName(): String = when (this) {
    NoteSortOrder.UPDATED_DESC -> "Recently Updated"
    NoteSortOrder.UPDATED_ASC -> "Oldest Updated"
    NoteSortOrder.TITLE_ASC -> "Title A → Z"
    NoteSortOrder.TITLE_DESC -> "Title Z → A"
    NoteSortOrder.CREATED_DESC -> "Newest Created"
}