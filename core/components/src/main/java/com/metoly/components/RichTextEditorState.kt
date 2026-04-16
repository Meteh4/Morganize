// RichTextEditorState.kt
package com.metoly.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType
import com.metoly.morganize.core.model.grid.TextAlignment

/**
 * State for the rich text editor.
 *
 * Formatting is stored as a list of non-overlapping [RichSpan] objects.
 * Two modes coexist:
 *   - **Selection mode** – the user has text selected; toggle applies/removes
 *     the span across [selection.min, selection.max).
 *   - **Typing-intent mode** – cursor only (collapsed selection); the toggle
 *     sets [pendingFormats] so the *next* characters typed inherit the format.
 */
data class RichTextEditorState(
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val spans: List<RichSpan> = emptyList(),
    // Typing-intent formats (applies to next typed characters when no selection)
    val pendingFormats: Set<SpanFormatType> = emptySet(),
    val isBulletListActive: Boolean = false,
    val isNumberedListActive: Boolean = false,
    // Typography
    val fontSize: Float = 14f,
    val textAlign: TextAlignment = TextAlignment.Start,
    val lineHeight: Float = 1.4f,
) {
    val text: String get() = textFieldValue.text
    val cursor: Int get() = textFieldValue.selection.start
    val selection: TextRange get() = textFieldValue.selection
    val isInListMode: Boolean get() = isBulletListActive || isNumberedListActive

    val hasSelection: Boolean get() = !selection.collapsed

    /**
     * Is the format active for the current state?
     *
     * - If text is selected → true if the *entire* selection is covered.
     * - If cursor only → true if a pending format is set OR the character
     *   immediately to the left of the cursor is inside a span of this type.
     */
    fun isFormatActive(type: SpanFormatType): Boolean {
        if (type == SpanFormatType.BULLET_LIST) return isBulletListActive
        if (type == SpanFormatType.NUMBERED_LIST) return isNumberedListActive

        return if (hasSelection) {
            isRangeFullyCovered(selection.min, selection.max, type)
        } else {
            // Pending intent OR cursor inside a span
            type in pendingFormats || isCursorInsideSpan(cursor, type)
        }
    }

    /** True if [start, end) is fully covered by spans of [type]. */
    private fun isRangeFullyCovered(start: Int, end: Int, type: SpanFormatType): Boolean {
        if (start >= end) return false
        // Collect all spans of this type, sorted
        val relevant = spans
            .filter { it.type == type && it.start < end && it.end > start }
            .sortedBy { it.start }

        var covered = start
        for (span in relevant) {
            if (span.start > covered) return false // gap
            covered = maxOf(covered, span.end)
            if (covered >= end) return true
        }
        return false
    }

    /** True if the cursor (exclusive end) is inside a span of [type]. */
    private fun isCursorInsideSpan(pos: Int, type: SpanFormatType): Boolean =
        spans.any { it.type == type && it.start < pos && it.end >= pos }
}

// ---------------------------------------------------------------------------
// Toggle formatting (selection-aware)
// ---------------------------------------------------------------------------

/**
 * Toggle [type] formatting.
 *
 * - **With selection**: adds / removes span across the selected range.
 * - **Without selection**: toggles the pending typing-intent flag.
 */
fun RichTextEditorState.toggleFormat(type: SpanFormatType): RichTextEditorState {
    if (type == SpanFormatType.BULLET_LIST) {
        return copy(
            isBulletListActive = !isBulletListActive,
            isNumberedListActive = false
        )
    }
    if (type == SpanFormatType.NUMBERED_LIST) {
        return copy(
            isNumberedListActive = !isNumberedListActive,
            isBulletListActive = false
        )
    }

    return if (hasSelection) {
        val sel = selection
        if (isFormatActive(type)) {
            // Remove format from selected range
            copy(spans = removeSpanRange(spans, sel.min, sel.max, type))
        } else {
            // Add format to selected range
            copy(spans = addSpanRange(spans, sel.min, sel.max, type))
        }
    } else {
        // Toggle typing intent
        val newPending = if (type in pendingFormats) pendingFormats - type else pendingFormats + type
        copy(pendingFormats = newPending)
    }
}

// ---------------------------------------------------------------------------
// Applying pending formats as the user types
// ---------------------------------------------------------------------------

/**
 * Call this from `onValueChange` every time the [TextFieldValue] changes.
 * Handles:
 *   - Extending pending-format spans when the user types a new character.
 *   - Clearing pending formats when selection becomes a range (user selects text).
 *   - Adjusting / removing spans when text is deleted.
 */
fun RichTextEditorState.handleTextChange(
    newTfv: TextFieldValue
): RichTextEditorState {
    val oldText = text
    val newText = newTfv.text

    var updatedSpans = spans

    if (newText.length > oldText.length) {
        // --- Insertion ---
        val insertStart = findInsertStart(oldText, newText)
        val insertEnd = insertStart + (newText.length - oldText.length)

        // Shift spans that come after the insert point
        updatedSpans = updatedSpans.map { span ->
            when {
                span.end <= insertStart -> span
                span.start >= insertStart -> span.copy(start = span.start + (insertEnd - insertStart), end = span.end + (insertEnd - insertStart))
                else -> span.copy(end = span.end + (insertEnd - insertStart)) // span straddles insert
            }
        }

        // Extend or add pending-format spans for the new characters
        if (pendingFormats.isNotEmpty()) {
            for (fmt in pendingFormats) {
                updatedSpans = extendOrAddSpan(updatedSpans, insertStart, insertEnd, fmt)
            }
        }
    } else if (newText.length < oldText.length) {
        // --- Deletion ---
        val deleteStart = findDeleteStart(oldText, newText)
        val deleteEnd = deleteStart + (oldText.length - newText.length)

        updatedSpans = updatedSpans
            .mapNotNull { span ->
                when {
                    // Span entirely before delete → unchanged
                    span.end <= deleteStart -> span
                    // Span entirely within deleted range → drop it
                    span.start >= deleteStart && span.end <= deleteEnd -> null
                    // Span entirely after delete → shift left
                    span.start >= deleteEnd -> span.copy(
                        start = span.start - (deleteEnd - deleteStart),
                        end = span.end - (deleteEnd - deleteStart)
                    )
                    // Span covers delete start but not end → shrink end
                    span.start < deleteStart && span.end <= deleteEnd ->
                        span.copy(end = deleteStart).takeIf { it.start < it.end }
                    // Span covers delete end but not start → shrink start
                    span.start >= deleteStart && span.end > deleteEnd ->
                        span.copy(start = deleteStart, end = span.end - (deleteEnd - deleteStart))
                    // Span fully encompasses delete → shrink
                    else -> span.copy(end = span.end - (deleteEnd - deleteStart))
                }
            }
            .filter { it.start < it.end }
    }

    // Clear pending formats if the user now has a real selection
    val newPending = if (!newTfv.selection.collapsed) emptySet() else pendingFormats

    return copy(
        textFieldValue = newTfv,
        spans = mergeAdjacentSpans(updatedSpans),
        pendingFormats = newPending
    )
}

// ---------------------------------------------------------------------------
// List continuation (Enter key in list mode)
// ---------------------------------------------------------------------------

fun RichTextEditorState.continueList(): RichTextEditorState {
    val cursorPos = cursor
    val currentText = text

    val (prefix, spanType) = when {
        isBulletListActive -> "• " to SpanFormatType.BULLET_LIST
        isNumberedListActive -> {
            val linesBefore = currentText.substring(0, cursorPos).lines()
            val nextNum = linesBefore.count { it.matches(Regex("^\\d+\\..*")) } + 1
            "$nextNum. " to SpanFormatType.NUMBERED_LIST
        }
        else -> return this
    }

    val newText = buildString {
        append(currentText, 0, cursorPos)
        append('\n')
        append(prefix)
        append(currentText, cursorPos, currentText.length)
    }
    val newCursor = cursorPos + 1 + prefix.length

    return copy(
        textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor)),
        spans = spans + RichSpan(start = cursorPos + 1, end = newCursor, type = spanType)
    )
}

// ---------------------------------------------------------------------------
// Restore from persisted data
// ---------------------------------------------------------------------------

fun richTextStateFromPersisted(
    text: String,
    spans: List<RichSpan>,
    fontSize: Float = 14f,
    textAlign: TextAlignment = TextAlignment.Start,
    lineHeight: Float = 1.4f,
): RichTextEditorState =
    RichTextEditorState(
        textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length)),
        spans = spans,
        isBulletListActive = spans.any { it.type == SpanFormatType.BULLET_LIST && it.end == text.length },
        isNumberedListActive = spans.any { it.type == SpanFormatType.NUMBERED_LIST && it.end == text.length },
        fontSize = fontSize,
        textAlign = textAlign,
        lineHeight = lineHeight,
    )

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/**
 * Add a new span [start, end) for [type], merging with any adjacent / overlapping spans.
 */
private fun addSpanRange(
    spans: List<RichSpan>,
    start: Int,
    end: Int,
    type: SpanFormatType
): List<RichSpan> {
    val others = spans.filter { it.type != type || it.end < start || it.start > end }
    val overlapping = spans.filter { it.type == type && it.end >= start && it.start <= end }
    val mergedStart = minOf(start, overlapping.minOfOrNull { it.start } ?: start)
    val mergedEnd = maxOf(end, overlapping.maxOfOrNull { it.end } ?: end)
    return (others + RichSpan(mergedStart, mergedEnd, type)).sortedWith(compareBy({ it.start }, { it.type.ordinal }))
}

/**
 * Remove span coverage for [type] in [start, end), splitting existing spans if needed.
 */
private fun removeSpanRange(
    spans: List<RichSpan>,
    start: Int,
    end: Int,
    type: SpanFormatType
): List<RichSpan> {
    val result = mutableListOf<RichSpan>()
    for (span in spans) {
        if (span.type != type || span.end <= start || span.start >= end) {
            result += span
            continue
        }
        // Left remnant
        if (span.start < start) result += RichSpan(span.start, start, type)
        // Right remnant
        if (span.end > end) result += RichSpan(end, span.end, type)
    }
    return result.sortedWith(compareBy({ it.start }, { it.type.ordinal }))
}

/**
 * Extend an existing span whose end == [insertStart] or add a new one [insertStart, insertEnd).
 */
private fun extendOrAddSpan(
    spans: List<RichSpan>,
    insertStart: Int,
    insertEnd: Int,
    type: SpanFormatType
): List<RichSpan> {
    val idx = spans.indexOfLast { it.type == type && it.end == insertStart }
    return if (idx >= 0) {
        spans.toMutableList().also { it[idx] = it[idx].copy(end = insertEnd) }
    } else {
        spans + RichSpan(insertStart, insertEnd, type)
    }
}

/** Find the character index where insertion happened (first differing position). */
private fun findInsertStart(old: String, new: String): Int {
    val minLen = minOf(old.length, new.length)
    for (i in 0 until minLen) if (old[i] != new[i]) return i
    return minLen
}

/** Find the character index where deletion happened. */
private fun findDeleteStart(old: String, new: String): Int {
    val minLen = minOf(old.length, new.length)
    for (i in 0 until minLen) if (old[i] != new[i]) return i
    return minLen
}

/** Merge consecutive spans of the same type that are adjacent or overlapping. */
private fun mergeAdjacentSpans(spans: List<RichSpan>): List<RichSpan> {
    val sorted = spans.sortedWith(compareBy({ it.type.ordinal }, { it.start }))
    val result = mutableListOf<RichSpan>()
    for (span in sorted) {
        val last = result.lastOrNull()
        if (last != null && last.type == span.type && last.end >= span.start) {
            result[result.size - 1] = last.copy(end = maxOf(last.end, span.end))
        } else {
            result += span
        }
    }
    return result
}