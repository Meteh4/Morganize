package com.metoly.morganize.core.data

import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for abstracting [Note] persistent storage layer.
 */
interface NoteRepository {

    /** Emit the full list of active notes (excludes deleted & archived), pinned first. */
    fun getAllNotes(): Flow<ResponseState<List<Note>>>

    /** Emit notes filtered by [categoryId], pinned first. */
    fun getNotesByCategory(categoryId: Long): Flow<ResponseState<List<Note>>>

    /** Search active notes by title. */
    fun searchNotes(query: String): Flow<ResponseState<List<Note>>>

    /** Emit a single note by id, or null if not found. */
    fun getNoteById(id: Long): Flow<ResponseState<Note?>>

    /** Emit all soft-deleted notes (trash bin). */
    fun getDeletedNotes(): Flow<ResponseState<List<Note>>>

    /** Emit all archived notes. */
    fun getArchivedNotes(): Flow<ResponseState<List<Note>>>

    /** Insert a new note. Returns the generated row id wrapped in ResponseState. */
    fun insertNote(note: Note): Flow<ResponseState<Long>>

    /** Update an existing note. */
    fun updateNote(note: Note): Flow<ResponseState<Unit>>

    /** Hard-delete a note by its entity. */
    fun deleteNote(note: Note): Flow<ResponseState<Unit>>

    /** Hard-delete a note by id. */
    fun deleteNoteById(id: Long): Flow<ResponseState<Unit>>

    /** Toggle pin status for a note. */
    fun setPinned(id: Long, isPinned: Boolean): Flow<ResponseState<Unit>>

    /** Soft-delete a note (move to trash). */
    fun softDelete(id: Long): Flow<ResponseState<Unit>>

    /** Restore a note from trash. */
    fun restoreNote(id: Long): Flow<ResponseState<Unit>>

    /** Toggle archive status for a note. */
    fun setArchived(id: Long, isArchived: Boolean): Flow<ResponseState<Unit>>

    /** Duplicate an existing note by id. */
    fun duplicateNote(id: Long): Flow<ResponseState<Long>>
}
