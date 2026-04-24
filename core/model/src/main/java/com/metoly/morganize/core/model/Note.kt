package com.metoly.morganize.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.metoly.morganize.core.model.grid.NotePage
import kotlinx.serialization.Serializable

/**
 * Core data model representing a note in the application. Annotated with [@Entity] for Room
 * persistence and [@Serializable] for navigation argument serialization.
 *
 * - [pages] : List of [com.metoly.morganize.core.model.grid.NotePage] objects describing the grid contents and pages. (Stored as JSON in DB)
 * - [backgroundColor] : ARGB colour int applied to the note card; null = default theme
 * - [categoryId] : FK to [Category.id]; null = uncategorised
 */
@Serializable
@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val title: String,
        @ColumnInfo(name = "pagesJson") val pages: List<NotePage> = emptyList(),
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val backgroundColor: Int? = null,
        val categoryId: Long? = null,
        // ── Secret Note fields ──────────────────────────────────────────
        val isSecret: Boolean = false,
        val encryptedContent: String? = null,   // Base64 AES-256-GCM ciphertext of pages JSON
        val salt: String? = null,               // Base64 PBKDF2 salt
        val iv: String? = null                  // Base64 GCM nonce
)
