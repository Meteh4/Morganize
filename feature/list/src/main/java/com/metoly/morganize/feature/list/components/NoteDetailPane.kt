package com.metoly.morganize.feature.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metoly.components.grid.DrawingCanvas
import com.metoly.components.grid.GridCanvas
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R
import com.metoly.morganize.feature.list.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteDetailPane(
    note: Note,
    category: Category?,
    showBackButton: Boolean,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = note.pages

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        note.title.ifBlank { stringResource(R.string.feature_list_untitled) },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.feature_list_back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = stringResource(R.string.feature_list_edit_note)
                        )
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            if (category != null) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(category.colorArgb)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }

            pages.forEachIndexed { index, page ->
                Text(
                    text = "Page ${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    GridCanvas(
                        page = page,
                        selectedItemId = null,
                        isReadOnly = true
                    )

                    if (page.strokes.isNotEmpty()) {
                        DrawingCanvas(
                            strokes = page.strokes,
                            isActive = false,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(
                    R.string.feature_list_last_updated,
                    DateFormatter.formatWithTime(note.updatedAt)
                ),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}