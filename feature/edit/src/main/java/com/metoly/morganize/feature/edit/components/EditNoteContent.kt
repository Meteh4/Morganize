package com.metoly.morganize.feature.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.ChecklistItemUi
import com.metoly.components.NoteContent
import com.metoly.morganize.feature.edit.R
import com.metoly.morganize.feature.edit.model.EditEvent
import com.metoly.morganize.feature.edit.model.EditUiState

@Composable
internal fun EditNoteContent(
    uiState: EditUiState,
    onEvent: (EditEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val checklistItemsUi = remember(uiState.checklistItems) {
        uiState.checklistItems.map { ChecklistItemUi(it.text, it.isChecked) }
    }

    NoteContent(
        title                      = uiState.title,
        onTitleChange              = { onEvent(EditEvent.TitleChanged(it)) },
        titleHint                  = stringResource(R.string.feature_edit_title_hint),
        richTextState              = uiState.richTextState,
        onRichTextChange           = { onEvent(EditEvent.RichTextChanged(it)) },
        onEnterPressed             = { onEvent(EditEvent.ContinueList) },
        contentHint                = stringResource(R.string.feature_edit_content_hint),
        categories                 = uiState.categories,
        selectedCategoryId         = uiState.categoryId,
        onCategorySelected         = { onEvent(EditEvent.CategorySelected(it)) },
        imagePaths                 = uiState.imagePaths,
        onImageRemoved             = { onEvent(EditEvent.ImageRemoved(it)) },
        drawingPath                = uiState.drawingPath,
        onDrawingRemoved           = { onEvent(EditEvent.DrawingChanged(null)) },
        checklistItems             = checklistItemsUi,
        onChecklistItemToggled     = { onEvent(EditEvent.ChecklistItemToggled(it)) },
        onChecklistItemTextChanged = { i, t -> onEvent(EditEvent.ChecklistItemTextChanged(i, t)) },
        onChecklistItemRemoved     = { onEvent(EditEvent.ChecklistItemRemoved(it)) },
        modifier                   = modifier
    )
}