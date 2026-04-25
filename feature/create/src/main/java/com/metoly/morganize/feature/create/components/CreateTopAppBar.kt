package com.metoly.morganize.feature.create.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.metoly.components.NoteTopBar
import com.metoly.morganize.feature.create.R

@Composable
internal fun CreateTopBar(
    onBack: () -> Unit,
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    isSecretNote: Boolean,
    onToggleSecretNote: () -> Unit
) {
    NoteTopBar(
        title = stringResource(R.string.feature_create_screen_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.feature_create_back),
        selectedColor = selectedColor,
        onColorSelected = onColorSelected,
        isSecretNote = isSecretNote,
        onToggleSecretNote = onToggleSecretNote
    )
}