package com.metoly.morganize.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the 'categories' table. This is the persistence layer representation.
 * Domain layer should use [com.metoly.morganize.core.model.Category] instead.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int
)
