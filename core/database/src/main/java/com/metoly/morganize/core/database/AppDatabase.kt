package com.metoly.morganize.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Converters
import com.metoly.morganize.core.model.Note

/** Main Room database for the Morganize application. */
@Database(entities = [Note::class, Category::class], version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "morganize_db"
    }
}
