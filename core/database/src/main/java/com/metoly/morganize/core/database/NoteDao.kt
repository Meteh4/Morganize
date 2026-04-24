package com.metoly.morganize.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.metoly.morganize.core.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for governing [Note] entity operations.
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC") fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId ORDER BY updatedAt DESC")
    fun getNotesByCategory(categoryId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id") fun getNoteById(id: Long): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNote(note: Note): Long

    @Update suspend fun updateNote(note: Note)

    @Delete suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id") suspend fun deleteNoteById(id: Long)
}
