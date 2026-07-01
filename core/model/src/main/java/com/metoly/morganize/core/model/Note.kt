package com.metoly.morganize.core.model

import com.metoly.morganize.core.model.grid.NotePage
import kotlinx.serialization.Serializable

/**
 * Domain model representing a note in the application.
 * Annotated with [@Serializable] for navigation argument serialization.
 *
 * This is a pure domain object — persistence is handled by
 * [com.metoly.morganize.core.database.entity.NoteEntity] in the database layer.
 *
 * - [pages] : List of [NotePage] objects describing the grid contents and pages.
 * - [backgroundColor] : ARGB colour int applied to the note card; null = default theme
 * - [categoryId] : FK to [Category.id]; null = uncategorised
 */
@Serializable
data class Note(
    val id: Long = 0,
    val title: String,
    val pages: List<NotePage> = emptyList(),
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
    // Faz 1 & 2 features
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    // Faz 3
    val reminderAt: Long? = null
) {
    fun toShareText(): String {
        if (isSecret) return "This note is locked and cannot be shared."
        
        val builder = java.lang.StringBuilder()
        if (title.isNotBlank()) {
            builder.appendLine(title)
            builder.appendLine()
        }
        
        pages.forEach { page ->
            page.items.forEach { item ->
                when (item) {
                    is com.metoly.morganize.core.model.grid.GridItem.Text -> {
                        if (item.textContent.isNotBlank()) {
                            builder.appendLine(item.textContent)
                        }
                    }
                    is com.metoly.morganize.core.model.grid.GridItem.Checklist -> {
                        if (item.title.isNotBlank()) builder.appendLine(item.title)
                        item.entries.forEach { entry ->
                            val check = if (entry.isChecked) "[x]" else "[ ]"
                            builder.appendLine("$check ${entry.text}")
                        }
                    }
                    else -> {}
                }
            }
        }
        
        return builder.toString().trimEnd()
    }
}
