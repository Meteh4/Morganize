package com.metoly.components

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
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorState
import com.metoly.morganize.core.model.SpanFormatType

/**
 * Aggregated bottom bar for the Create and Edit screens.
 * Contains toolbars for Rich Text formatting, Drawing configuration, and the main NoteBottomBar actions.
 * Displays the appropriate toolbar conditionally based on the active editing mode.
 *
 * @param state Current note editor state.
 * @param activePageIndex The index of the currently viewed grid page.
 * @param activeEditingTextItemId The ID of the text item currently being edited.
 * @param activeRichState The rich text formatting state of the active text item.
 * @param onRichStateUpdate Callback to sink rich text formatting changes.
 * @param onEvent General NoteEditorEvent callback.
 * @param onAddSecretItem Callback to initiate secret item creation flow.
 * @param onSave Callback to save the note.
 * @param saveContentDescription Accessibility description for the save button.
 * @param imagePickerLauncher Launcher to handle visual media selection.
 */
@Composable
fun NoteEditorBottomBar(
    state: NoteEditorState,
    activePageIndex: Int,
    activeEditingTextItemId: String?,
    activeRichState: RichTextEditorState?,
    onRichStateUpdate: (RichTextEditorState) -> Unit,
    onEvent: (NoteEditorEvent) -> Unit,
    onAddSecretItem: () -> Unit,
    onSave: () -> Unit,
    saveContentDescription: String,
    imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>
) {
    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = activeEditingTextItemId != null && activeRichState != null && !state.isDrawingMode,
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

        AnimatedVisibility(
            visible = state.isDrawingMode,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val canUndoDrawing = state.pages.any { it.strokes.isNotEmpty() }

            DrawingToolbar(
                penColorArgb = state.drawingPenColorArgb,
                strokeWidthFraction = state.drawingStrokeWidthFraction,
                eraserWidthFraction = state.drawingEraserWidthFraction,
                isEraserMode = state.isEraserMode,
                canUndo = canUndoDrawing,
                onColorSelected = { onEvent(NoteEditorEvent.DrawingColorChanged(it)) },
                onStrokeWidthChange = { widthFraction ->
                    onEvent(NoteEditorEvent.DrawingStrokeWidthChanged(widthFraction))
                },
                onEraserWidthChange = { widthFraction ->
                    onEvent(NoteEditorEvent.DrawingEraserWidthChanged(widthFraction))
                },
                onToggleEraser = { onEvent(NoteEditorEvent.DrawingEraserToggled) },
                onUndo = {
                    state.pages
                        .filter { it.strokes.isNotEmpty() }
                        .forEach { page ->
                            onEvent(NoteEditorEvent.DrawingStrokeReverted(pageId = page.id))
                        }
                }
            )
        }

        NoteBottomBar(
            isDrawingMode = state.isDrawingMode,
            onAddText = { onEvent(NoteEditorEvent.TextGridItemAdded("", activePageIndex)) },
            onAddImage = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onAddChecklist = { onEvent(NoteEditorEvent.ChecklistGridItemAdded(activePageIndex)) },
            onAddSecretItem = onAddSecretItem,
            onStartDrawing = { onEvent(NoteEditorEvent.DrawingModeToggled) },
            onSave = onSave,
            saveContentDescription = saveContentDescription
        )
    }
}
