package com.metoly.morganize.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.metoly.morganize.core.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for [NoteEntity] operations.
 * Returns raw entities — mapping to domain models is done in the repository layer.
 *
 * All list queries exclude soft-deleted notes (isDeleted = 0) unless explicitly querying trash.
 * Pinned notes are sorted to the top by default.
 */
@Dao
interface NoteDao {

    /** Active notes ordered: pinned first, then by updatedAt DESC. */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /** Active notes filtered by category, pinned first. */
    @Query("SELECT * FROM notes WHERE categoryId = :categoryId AND isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(categoryId: Long): Flow<List<NoteEntity>>

    /** Search notes by title (case-insensitive partial match). */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND title LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    /** Single note by id. */
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    /** All soft-deleted notes, ordered by deletion time DESC. */
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    /** All archived notes, pinned first. */
    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    /** Toggle pin status. */
    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    /** Soft-delete (move to trash). */
    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    /** Restore from trash. */
    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: Long)

    /** Toggle archive status. */
    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    /** Permanently delete all trashed notes older than [cutoffTime]. */
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :cutoffTime")
    suspend fun purgeOldDeletedNotes(cutoffTime: Long)
}
