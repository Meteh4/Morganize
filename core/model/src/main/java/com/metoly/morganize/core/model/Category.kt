package com.metoly.morganize.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a user-defined category that can be attached to a [Note]. Stored in its own Room table
 * and linked to notes via [Note.categoryId].
 */
@Serializable
@Entity(tableName = "categories")
data class Category(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val name: String,
        /** ARGB colour int used to visually distinguish categories. */
        val colorArgb: Int
)
