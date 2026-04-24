package com.metoly.morganize.feature.list.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metoly.components.CategoryChipRow
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R

/**
 * The primary master screen portion of the dual-pane List layout.
 * Hosts the horizontal category filters, note creation fab, and the actual vertical list of notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteListPane(
    notes: List<Note>,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    selectedNoteId: Long?,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onCreateNote: () -> Unit,
    onAddCategory: (() -> Unit)? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.feature_list_app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(
                        R.string.feature_list_create_note
                    )
                )
            }
        }
    ) { padding ->
        if (notes.isEmpty() && categories.isEmpty() && !isLoading) {
            EmptyListContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            androidx.compose.foundation.layout.Column(
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
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), 
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (notes.isEmpty()) {
                    EmptyListContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
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
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    fadeOutSpec = spring(
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}