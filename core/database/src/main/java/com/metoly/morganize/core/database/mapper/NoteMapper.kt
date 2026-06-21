package com.metoly.morganize.core.database.mapper

import com.metoly.morganize.core.database.entity.NoteEntity
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.grid.NotePage
import kotlinx.serialization.json.Json

/**
 * Bidirectional mapper between [NoteEntity] (persistence) and [Note] (domain).
 *
 * The [json] instance should be DI-provided with `ignoreUnknownKeys = true` to handle
 * forward-compatible schema evolution of the pages JSON.
 */
class NoteMapper(private val json: Json) {

    fun toDomain(entity: NoteEntity): Note = Note(
        id = entity.id,
        title = entity.title,
        pages = deserializePages(entity.pagesJson),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        backgroundColor = entity.backgroundColor,
        categoryId = entity.categoryId,
        isSecret = entity.isSecret,
        encryptedContent = entity.encryptedContent,
        salt = entity.salt,
        iv = entity.iv,
        hasBiometric = entity.hasBiometric,
        biometricWrappedPassword = entity.biometricWrappedPassword,
        biometricWrappedPasswordIv = entity.biometricWrappedPasswordIv,
        isPinned = entity.isPinned,
        isDeleted = entity.isDeleted,
        deletedAt = entity.deletedAt,
        isArchived = entity.isArchived,
        reminderAt = entity.reminderAt
    )

    fun toEntity(domain: Note): NoteEntity = NoteEntity(
        id = domain.id,
        title = domain.title,
        pagesJson = serializePages(domain.pages),
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
        backgroundColor = domain.backgroundColor,
        categoryId = domain.categoryId,
        isSecret = domain.isSecret,
        encryptedContent = domain.encryptedContent,
        salt = domain.salt,
        iv = domain.iv,
        hasBiometric = domain.hasBiometric,
        biometricWrappedPassword = domain.biometricWrappedPassword,
        biometricWrappedPasswordIv = domain.biometricWrappedPasswordIv,
        isPinned = domain.isPinned,
        isDeleted = domain.isDeleted,
        deletedAt = domain.deletedAt,
        isArchived = domain.isArchived,
        reminderAt = domain.reminderAt
    )

    fun toDomainList(entities: List<NoteEntity>): List<Note> = entities.map { toDomain(it) }

    private fun deserializePages(pagesJson: String): List<NotePage> =
        if (pagesJson.isBlank()) emptyList()
        else json.decodeFromString(pagesJson)

    private fun serializePages(pages: List<NotePage>): String =
        if (pages.isEmpty()) "[]"
        else json.encodeToString(pages)
}
