package com.metoly.morganize.feature.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.metoly.components.parseMarkdown
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ChecklistItem
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R
import com.metoly.morganize.feature.list.util.DateFormatter
import kotlinx.serialization.json.Json

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
    val checklistItems = remember(note.checklistJson) {
        runCatching {
            if (note.checklistJson.isNotBlank()) {
                Json.decodeFromString<List<ChecklistItem>>(note.checklistJson)
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        note.title.ifBlank {
                            stringResource(R.string.feature_list_untitled)
                        },
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
                .padding(24.dp)
        ) {
            if (category != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Label,
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

            if (note.imagePaths.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(note.imagePaths) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = "Not görseli",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (note.drawingPath != null) {
                AsyncImage(
                    model = note.drawingPath,
                    contentDescription = "Çizim",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(16.dp))
            }

            if (note.content.isNotBlank()) {
                if (note.isMarkdownEnabled) {
                    Text(
                        text = parseMarkdown(note.content),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(16.dp))
            } else if (note.hasNoDisplayContent(checklistItems)) {
                Text(
                    text = stringResource(R.string.feature_list_no_content),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            if (checklistItems.isNotEmpty()) {
                Column {
                    checklistItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun Note.hasNoDisplayContent(checklistItems: List<ChecklistItem>): Boolean =
    content.isBlank() &&
            drawingPath == null &&
            imagePaths.isEmpty() &&
            checklistItems.isEmpty()