// RichTextEditor.kt
package com.metoly.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType

@Composable
fun RichTextEditor(
    state: RichTextEditorState,
    onStateChange: (RichTextEditorState) -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier,
    baseTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    enabled: Boolean = true,
    hint: @Composable (() -> Unit)? = null
) {
    val textAlign = when (state.textAlign) {
        "Center" -> TextAlign.Center
        "End" -> TextAlign.End
        else -> TextAlign.Start
    }

    val textStyle = baseTextStyle.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = state.fontSize.sp,
        lineHeight = (state.fontSize * state.lineHeight).sp,
        textAlign = textAlign
    )

    val annotated = remember(state.text, state.spans) {
        buildAnnotatedFromSpans(state.text, state.spans)
    }

    val displayValue = remember(annotated, state.textFieldValue.selection) {
        TextFieldValue(annotatedString = annotated, selection = state.textFieldValue.selection)
    }

    BasicTextField(
        value = displayValue,
        onValueChange = { newValue ->
            // Handle list Enter-key continuation
            if (state.isInListMode && newValue.text.length > state.text.length) {
                val cursorPos = newValue.selection.start
                if (cursorPos > 0 && newValue.text[cursorPos - 1] == '\n') {
                    onEnterPressed()
                    return@BasicTextField
                }
            }
            val newState = state.handleTextChange(newValue)
            onStateChange(newState)
        },
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        decorationBox = { innerTextField ->
            if (state.text.isEmpty() && hint != null) hint()
            innerTextField()
        },
        modifier = modifier
    )
}

fun buildAnnotatedFromSpans(plain: String, spans: List<RichSpan>): AnnotatedString =
    buildAnnotatedString {
        append(plain)
        for (span in spans) {
            val start = span.start.coerceIn(0, plain.length)
            val end = span.end.coerceIn(start, plain.length)
            if (start >= end) continue
            val style = when (span.type) {
                SpanFormatType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                SpanFormatType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                SpanFormatType.BULLET_LIST,
                SpanFormatType.NUMBERED_LIST -> SpanStyle(fontWeight = FontWeight.Normal)
            }
            addStyle(style, start, end)
        }
    }