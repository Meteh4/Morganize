package com.metoly.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType

/**
 * Encapsulates the full state of the rich text editor.
 *
 * [textFieldValue] tracks text + cursor position (Compose TextField contract)
 * [spans] is the list of active formatting ranges on the plain text
 * [isBoldActive] / [isItalicActive] / ... are toggle flags for the current insertion mode
 * [isBulletListActive] / [isNumberedListActive] track the current list mode
 */
data class RichTextEditorState(
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val spans: List<RichSpan> = emptyList(),
    val isBoldActive: Boolean = false,
    val isItalicActive: Boolean = false,
    val isBulletListActive: Boolean = false,
    val isNumberedListActive: Boolean = false
) {
    /** Convenience accessor for the plain text string. */
    val text: String get() = textFieldValue.text

    /** Convenience accessor for the current cursor position. */
    val cursor: Int get() = textFieldValue.selection.start
}

/**
 * Applies inline formatting (Bold / Italic) to the newly typed character range.
 *
 * Called right after the TextFieldValue changes to wrap the delta in the appropriate span.
 */
fun RichTextEditorState.applyInlineFormat(
    previous: RichTextEditorState,
    formatType: SpanFormatType
): RichTextEditorState {
    val oldText = previous.text
    val newText = text
    if (newText.length <= oldText.length) return this

    val insertStart = previous.cursor.coerceAtMost(cursor)
    val insertEnd = cursor

    val mutableSpans = spans.toMutableList()
    val lastSameType = mutableSpans.lastOrNull { it.type == formatType && it.end == insertStart }
    if (lastSameType != null) {
        val idx = mutableSpans.indexOf(lastSameType)
        mutableSpans[idx] = lastSameType.copy(end = insertEnd)
    } else {
        mutableSpans.add(RichSpan(start = insertStart, end = insertEnd, type = formatType))
    }

    return copy(spans = mutableSpans)
}

fun RichTextEditorState.continueList(): RichTextEditorState {
    val cursorPos = textFieldValue.selection.start
    val currentText = textFieldValue.text

    val prefix: String
    val spanType: SpanFormatType

    when {
        isBulletListActive -> {
            prefix = "• "
            spanType = SpanFormatType.BULLET_LIST
        }
        isNumberedListActive -> {
            val linesBefore = currentText.substring(0, cursorPos).lines()
            val nextNum = linesBefore.count { it.matches(Regex("^\\d+\\..*")) } + 1
            prefix = "$nextNum. "
            spanType = SpanFormatType.NUMBERED_LIST
        }
        else -> return this
    }

    val newText = currentText.substring(0, cursorPos) + "\n" + prefix + currentText.substring(cursorPos)
    val newCursor = cursorPos + 1 + prefix.length

    val newSpan = RichSpan(start = cursorPos + 1, end = newCursor, type = spanType)
    val updatedSpans = spans + newSpan

    return copy(
        textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor)),
        spans = updatedSpans
    )
}

/**
 * Builds a [RichTextEditorState] from persisted plain text + JSON span data.
 * Used by ViewModels when loading a note from the database.
 */
fun richTextStateFromPersisted(text: String, spans: List<RichSpan>): RichTextEditorState {
    return RichTextEditorState(
        textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length)),
        spans = spans,
        isBulletListActive = spans.any { it.type == SpanFormatType.BULLET_LIST && it.end == text.length },
        isNumberedListActive = spans.any { it.type == SpanFormatType.NUMBERED_LIST && it.end == text.length }
    )
}
