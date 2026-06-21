package com.metoly.morganize.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.metoly.morganize.core.database.entity.CategoryEntity
import com.metoly.morganize.core.database.entity.NoteEntity
import com.metoly.morganize.core.database.entity.NoteTagCrossRef
import com.metoly.morganize.core.database.entity.TagEntity

/**
 * Main Room database for the Morganize application.
 * Uses dedicated entity classes ([NoteEntity], [CategoryEntity]) — domain models
 * are mapped in the repository layer via mappers.
 */
@Database(
    entities = [NoteEntity::class, CategoryEntity::class, TagEntity::class, NoteTagCrossRef::class],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "morganize_db"
    }
}
