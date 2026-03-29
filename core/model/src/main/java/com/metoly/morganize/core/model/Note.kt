package com.metoly.morganize.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

/**
 * Core data model representing a note in the application. Annotated with [@Entity] for Room
 * persistence and [@Serializable] for navigation argument serialization.
 *
 * - [pagesJson] : JSON-serialized list of [com.metoly.morganize.core.model.grid.NotePage] objects describing the grid contents and pages.
 * - [backgroundColor] : ARGB colour int applied to the note card; null = default theme
 * - [categoryId] : FK to [Category.id]; null = uncategorised
 */
@Serializable
@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val title: String,
        val pagesJson: String = "[]",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val backgroundColor: Int? = null,
        val categoryId: Long? = null
)
