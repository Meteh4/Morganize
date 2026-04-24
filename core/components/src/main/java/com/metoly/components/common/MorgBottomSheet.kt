package com.metoly.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.metoly.morganize.core.ui.theme.MorgDimens

/**
 * Standard bottom sheet wrapper used by every sheet in the app.
 *
 * Enforces:
 * - `skipPartiallyExpanded = true`
 * - No drag handle (content should use [MorgSheetHeader] instead)
 * - Consistent surface colour and 24dp content padding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorgBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MorgDimens.sheetPadding),
            content = content
        )
    }
}
