package com.metoly.morganize.core.data

import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for abstracting [Note] persistent storage layer.
 */
interface NoteRepository {

    /** Emit the full list of notes ordered by last updated, reactively. */
    fun getAllNotes(): Flow<ResponseState<List<Note>>>

    /** Emit notes filtered by [categoryId], ordered by last updated. */
    fun getNotesByCategory(categoryId: Long): Flow<ResponseState<List<Note>>>

    /** Emit a single note by id, or null if not found. */
    fun getNoteById(id: Long): Flow<ResponseState<Note?>>

    /** Insert a new note. Returns the generated row id wrapped in ResponseState. */
    fun insertNote(note: Note): Flow<ResponseState<Long>>

    /** Update an existing note. */
    fun updateNote(note: Note): Flow<ResponseState<Unit>>

    /** Delete a note by its entity. */
    fun deleteNote(note: Note): Flow<ResponseState<Unit>>

    /** Delete a note by id. */
    fun deleteNoteById(id: Long): Flow<ResponseState<Unit>>
}
