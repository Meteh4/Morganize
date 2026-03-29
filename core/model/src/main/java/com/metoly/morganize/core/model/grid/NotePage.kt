package com.metoly.morganize.core.model.grid

import kotlinx.serialization.Serializable

/**
 * Represents a discrete page containing a grid layout of varying components.
 */
@Serializable
data class NotePage(
    val id: String,
    val items: List<GridItem> = emptyList()
)
