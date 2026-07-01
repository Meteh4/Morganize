package com.metoly.morganize.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the 'tags' table. Tags provide a lightweight,
 * many-to-many labelling system for notes (complementary to categories).
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ARGB colour int for the tag chip. */
    val colorArgb: Int
)
