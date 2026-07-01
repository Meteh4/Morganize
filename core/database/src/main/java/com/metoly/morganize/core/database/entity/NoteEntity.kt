package com.metoly.morganize.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the 'notes' table. This is the persistence layer representation.
 * Domain layer should use [com.metoly.morganize.core.model.Note] instead.
 *
 * Conversion between entity and domain is handled by [com.metoly.morganize.core.database.mapper.NoteMapper].
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "pagesJson") val pagesJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val backgroundColor: Int? = null,
    val categoryId: Long? = null,
    val isSecret: Boolean = false,
    val encryptedContent: String? = null,
    val salt: String? = null,
    val iv: String? = null,
    val hasBiometric: Boolean = false,
    val biometricWrappedPassword: String? = null,
    val biometricWrappedPasswordIv: String? = null,
    // Faz 1 & 2 columns (MIGRATION_6_7)
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    // Faz 3 column (MIGRATION_7_8)
    val reminderAt: Long? = null
)
