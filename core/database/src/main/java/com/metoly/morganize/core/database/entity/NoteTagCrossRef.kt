package com.metoly.morganize.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Cross-reference entity for the many-to-many relationship between Notes and Tags.
 */
@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
