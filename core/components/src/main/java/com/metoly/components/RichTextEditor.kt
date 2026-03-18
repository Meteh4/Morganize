package com.metoly.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
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
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.SpanFormatType

/**
 * Converts a flat list of [RichSpan]s into an [AnnotatedString] layered on [plain] text.
 * Spans are applied in order; overlapping spans are all applied independently.
 */
fun buildAnnotatedFromSpans(plain: String, spans: List<RichSpan>): AnnotatedString =
    buildAnnotatedString {
        append(plain)
        spans.forEach { span ->
            val start = span.start.coerceIn(0, plain.length)
            val end = span.end.coerceIn(start, plain.length)
            if (start >= end) return@forEach
            when (span.type) {
                SpanFormatType.BOLD ->
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                SpanFormatType.ITALIC ->
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                SpanFormatType.BULLET_LIST ->
                    addStyle(SpanStyle(fontWeight = FontWeight.Normal), start, end)
                SpanFormatType.NUMBERED_LIST ->
                    addStyle(SpanStyle(fontWeight = FontWeight.Normal), start, end)
            }
        }
    }


@Composable
fun RichTextEditor(
    state: RichTextEditorState,
    onStateChange: (TextFieldValue) -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    hint: @Composable (() -> Unit)? = null
) {
    val annotated = remember(state.text, state.spans) {
        buildAnnotatedFromSpans(state.text, state.spans)
    }

    val displayValue = remember(annotated, state.textFieldValue.selection) {
        TextFieldValue(
            annotatedString = annotated,
            selection = state.textFieldValue.selection
        )
    }

    val inListMode = state.isBulletListActive || state.isNumberedListActive
    val cursorColor = MaterialTheme.colorScheme.primary

    BasicTextField(
        value = displayValue,
        onValueChange = { newValue ->
            val newText = newValue.text
            val oldText = state.text

            if (inListMode && newText.length > oldText.length) {
                val cursorPos = newValue.selection.start
                if (cursorPos > 0 && newText[cursorPos - 1] == '\n') {
                    onEnterPressed()
                    return@BasicTextField
                }
            }
            onStateChange(newValue)
        },
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(cursorColor),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        decorationBox = { innerTextField ->
            if (state.text.isEmpty() && hint != null) {
                hint()
            }
            innerTextField()
        },
        modifier = modifier
    )
}
