package com.metoly.morganize.feature.create.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.ChecklistItemUi
import com.metoly.components.NoteContent
import com.metoly.morganize.feature.create.R
import com.metoly.morganize.feature.create.model.CreateEvent
import com.metoly.morganize.feature.create.model.CreateUiState

@Composable
internal fun CreateNoteContent(
    uiState: CreateUiState,
    onEvent: (CreateEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val checklistItemsUi = remember(uiState.checklistItems) {
        uiState.checklistItems.map { ChecklistItemUi(it.text, it.isChecked) }
    }

    NoteContent(
        title                      = uiState.title,
        onTitleChange              = { onEvent(CreateEvent.TitleChanged(it)) },
        titleHint                  = stringResource(R.string.feature_create_title_hint),
        richTextState              = uiState.richTextState,
        onRichTextChange           = { onEvent(CreateEvent.RichTextChanged(it)) },
        onEnterPressed             = { onEvent(CreateEvent.ContinueList) },
        contentHint                = stringResource(R.string.feature_create_content_hint),
        categories                 = uiState.categories,
        selectedCategoryId         = uiState.categoryId,
        onCategorySelected         = { onEvent(CreateEvent.CategorySelected(it)) },
        imagePaths                 = uiState.imagePaths,
        onImageRemoved             = { onEvent(CreateEvent.ImageRemoved(it)) },
        drawingPath                = uiState.drawingPath,
        onDrawingRemoved           = { onEvent(CreateEvent.DrawingChanged(null)) },
        checklistItems             = checklistItemsUi,
        onChecklistItemToggled     = { onEvent(CreateEvent.ChecklistItemToggled(it)) },
        onChecklistItemTextChanged = { i, t -> onEvent(CreateEvent.ChecklistItemTextChanged(i, t)) },
        onChecklistItemRemoved     = { onEvent(CreateEvent.ChecklistItemRemoved(it)) },
        modifier                   = modifier
    )
}