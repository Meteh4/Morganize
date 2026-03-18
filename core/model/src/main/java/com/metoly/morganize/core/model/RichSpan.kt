package com.metoly.morganize.core.model

import kotlinx.serialization.Serializable

/**
 * Supported rich-text formatting types.
 */
@Serializable
enum class SpanFormatType {
    BOLD,
    ITALIC,
    BULLET_LIST,
    NUMBERED_LIST
}

/**
 * Represents a single rich-text span within the note content.
 *
 * [start] and [end] are character-index offsets into the plain-text content string.
 * [type] is the formatting applied for that range.
 */
@Serializable
data class RichSpan(
    val start: Int,
    val end: Int,
    val type: SpanFormatType
)
