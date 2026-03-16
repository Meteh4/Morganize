package com.metoly.morganize.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

/**
 * Core data model representing a note in the application. Annotated with [@Entity] for Room
 * persistence and [@Serializable] for navigation argument serialization.
 *
 * - [imagePaths] : Stored image URIs / file paths (JSON list via TypeConverter)
 * - [drawingPath] : File path of the saved drawing Bitmap (null if no drawing)
 * - [isMarkdownEnabled] : Whether the content field contains Markdown markup
 * - [checklistJson] : JSON-serialized list of [ChecklistItem] objects
 * - [backgroundColor] : ARGB colour int applied to the note card; null = default theme
 * - [categoryId] : FK to [Category.id]; null = uncategorised
 */
@Serializable
@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val title: String,
        val content: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val backgroundColor: Int? = null,
        val categoryId: Long? = null,
        val imagePaths: List<String> = emptyList(),
        val drawingPath: String? = null,
        val isMarkdownEnabled: Boolean = false,
        val checklistJson: String = ""
)
