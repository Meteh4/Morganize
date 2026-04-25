package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.NoteDao
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.asResponseState
import com.metoly.morganize.core.model.suspendAsResponseStateFlow
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [NoteRepository] leveraging the Room [NoteDao].
 */
class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {

    override fun getAllNotes(): Flow<ResponseState<List<Note>>> =
            noteDao.getAllNotes().asResponseState()

    override fun getNotesByCategory(categoryId: Long): Flow<ResponseState<List<Note>>> =
            noteDao.getNotesByCategory(categoryId).asResponseState()

    override fun getNoteById(id: Long): Flow<ResponseState<Note?>> =
            noteDao.getNoteById(id).asResponseState()

    override fun insertNote(note: Note): Flow<ResponseState<Long>> = suspendAsResponseStateFlow {
        noteDao.insertNote(note)
    }

    override fun updateNote(note: Note): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.updateNote(note)
    }

    override fun deleteNote(note: Note): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.deleteNote(note)
    }

    override fun deleteNoteById(id: Long): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.deleteNoteById(id)
    }
}
