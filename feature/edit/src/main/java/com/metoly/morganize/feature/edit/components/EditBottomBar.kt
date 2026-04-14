package com.metoly.morganize.feature.edit.components

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.metoly.components.DrawingToolbar
import com.metoly.components.NoteBottomBar
import com.metoly.components.RichTextEditorState
import com.metoly.components.RichTextToolbar
import com.metoly.components.clampedFontSize
import com.metoly.components.nextLineHeight
import com.metoly.components.nextTextAlign
import com.metoly.components.toggleFormat
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.core.model.grid.NotePage
import com.metoly.morganize.feature.edit.R
import com.metoly.morganize.feature.edit.model.EditEvent

@Composable
internal fun EditBottomBar(
    pages: List<NotePage>,
    activePageIndex: Int,
    isDrawingMode: Boolean,
    isEraserMode: Boolean,
    drawingPenColorArgb: Long,
    drawingStrokeWidthFraction: Float,
    drawingEraserWidthFraction: Float,
    activeEditingTextItemId: String?,
    activeRichState: RichTextEditorState?,
    onRichStateUpdate: (RichTextEditorState) -> Unit,
    onEvent: (EditEvent) -> Unit,
    imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>
) {
    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth()
    ) {
        RichToolbarSection(
            activeEditingTextItemId = activeEditingTextItemId,
            activeRichState = activeRichState,
            isDrawingMode = isDrawingMode,
            onRichStateUpdate = onRichStateUpdate
        )

        DrawingToolbarSection(
            pages = pages,
            isDrawingMode = isDrawingMode,
            isEraserMode = isEraserMode,
            drawingPenColorArgb = drawingPenColorArgb,
            drawingStrokeWidthFraction = drawingStrokeWidthFraction,
            drawingEraserWidthFraction = drawingEraserWidthFraction,
            onEvent = onEvent
        )

        NoteBottomBar(
            isDrawingMode = isDrawingMode,
            onAddText = { onEvent(EditEvent.TextGridItemAdded("", activePageIndex)) },
            onAddImage = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onAddChecklist = { onEvent(EditEvent.ChecklistGridItemAdded(activePageIndex)) },
            onStartDrawing = { onEvent(EditEvent.DrawingModeToggled) },
            onSave = { onEvent(EditEvent.Save) },
            saveContentDescription = stringResource(R.string.feature_edit_save)
        )
    }
}

@Composable
private fun RichToolbarSection(
    activeEditingTextItemId: String?,
    activeRichState: RichTextEditorState?,
    isDrawingMode: Boolean,
    onRichStateUpdate: (RichTextEditorState) -> Unit
) {
    AnimatedVisibility(
        visible = activeEditingTextItemId != null && activeRichState != null && !isDrawingMode,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        activeRichState?.let { richState ->
            RichTextToolbar(
                state = richState,
                onToggleBold = { onRichStateUpdate(richState.toggleFormat(SpanFormatType.BOLD)) },
                onToggleItalic = { onRichStateUpdate(richState.toggleFormat(SpanFormatType.ITALIC)) },
                onToggleBulletList = { onRichStateUpdate(richState.toggleFormat(SpanFormatType.BULLET_LIST)) },
                onToggleNumberedList = { onRichStateUpdate(richState.toggleFormat(SpanFormatType.NUMBERED_LIST)) },
                onFontSizeIncrease = { onRichStateUpdate(richState.copy(fontSize = clampedFontSize(richState.fontSize, 2f))) },
                onFontSizeDecrease = { onRichStateUpdate(richState.copy(fontSize = clampedFontSize(richState.fontSize, -2f))) },
                onTextAlignCycle = { onRichStateUpdate(richState.copy(textAlign = nextTextAlign(richState.textAlign))) },
                onLineHeightCycle = { onRichStateUpdate(richState.copy(lineHeight = nextLineHeight(richState.lineHeight))) }
            )
        }
    }
}

@Composable
private fun DrawingToolbarSection(
    pages: List<NotePage>,
    isDrawingMode: Boolean,
    isEraserMode: Boolean,
    drawingPenColorArgb: Long,
    drawingStrokeWidthFraction: Float,
    drawingEraserWidthFraction: Float,
    onEvent: (EditEvent) -> Unit
) {
    AnimatedVisibility(
        visible = isDrawingMode,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val canUndoDrawing = pages.any { it.strokes.isNotEmpty() }

        DrawingToolbar(
            penColorArgb = drawingPenColorArgb,
            strokeWidthFraction = drawingStrokeWidthFraction,
            eraserWidthFraction = drawingEraserWidthFraction,
            isEraserMode = isEraserMode,
            canUndo = canUndoDrawing,
            onColorSelected = { onEvent(EditEvent.DrawingColorChanged(it)) },
            onStrokeWidthChange = { widthFraction ->
                onEvent(EditEvent.DrawingStrokeWidthChanged(widthFraction))
            },
            onEraserWidthChange = { widthFraction ->
                onEvent(EditEvent.DrawingEraserWidthChanged(widthFraction))
            },
            onToggleEraser = { onEvent(EditEvent.DrawingEraserToggled) },
            onUndo = {
                pages
                    .filter { it.strokes.isNotEmpty() }
                    .forEach { page ->
                        onEvent(EditEvent.DrawingStrokeReverted(pageId = page.id))
                    }
            }
        )
    }
}
