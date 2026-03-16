package com.metoly.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * A lightweight Markdown → [AnnotatedString] converter.
 *
 * Supported syntax:
 * - `## Heading` → bold + larger text
 * - `**bold**` → [FontWeight.Bold]
 * - `*italic*` → [FontStyle.Italic]
 * - `- item` → bullet point prefix
 * - `1. item` → numbered item (preserved as-is)
 */
fun parseMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val lines = text.lines()
    lines.forEachIndexed { index, line ->
        when {
            line.startsWith("## ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                    append(line.removePrefix("## "))
                }
            }
            line.startsWith("# ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)) {
                    append(line.removePrefix("# "))
                }
            }
            line.startsWith("- ") -> {
                append("• ")
                appendInlineMarkdown(line.removePrefix("- "))
            }
            else -> appendInlineMarkdown(line)
        }
        if (index < lines.lastIndex) append("\n")
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    // Regex to detect **bold** and *italic* spans
    val pattern = Regex("""\*\*(.+?)\*\*|\*(.+?)\*""")
    var lastEnd = 0
    pattern.findAll(text).forEach { match ->
        // Append plain text before the match
        if (match.range.first > lastEnd) {
            append(text.substring(lastEnd, match.range.first))
        }
        when {
            match.groupValues[1].isNotEmpty() -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[1]) }
            }
            match.groupValues[2].isNotEmpty() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[2]) }
            }
        }
        lastEnd = match.range.last + 1
    }
    if (lastEnd < text.length) append(text.substring(lastEnd))
}
