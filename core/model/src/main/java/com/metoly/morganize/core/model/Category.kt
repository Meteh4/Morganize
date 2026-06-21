package com.metoly.morganize.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a user-defined category that can be attached to a [Note].
 *
 * This is a pure domain object — persistence is handled by
 * [com.metoly.morganize.core.database.entity.CategoryEntity] in the database layer.
 */
@Serializable
data class Category(
    val id: Long = 0,
    val name: String,
    /** ARGB colour int used to visually distinguish categories. */
    val colorArgb: Int
)
