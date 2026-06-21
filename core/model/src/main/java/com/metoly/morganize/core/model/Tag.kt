package com.metoly.morganize.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a tag in the application.
 */
@Serializable
data class Tag(
    val id: Long = 0,
    val name: String,
    val colorArgb: Int
)
