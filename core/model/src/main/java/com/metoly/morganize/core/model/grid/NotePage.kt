package com.metoly.morganize.core.model.grid

import kotlinx.serialization.Serializable

/**
 * Represents a discrete page containing a grid layout of varying components.
 * [strokes] holds the list of [DrawingStroke]s for the
 * full-page freehand drawing layer that sits on top of all grid items.
 */
@Serializable
data class NotePage(
    val id: String,
    val items: List<GridItem> = emptyList(),
    val strokes: List<DrawingStroke> = emptyList()
)
