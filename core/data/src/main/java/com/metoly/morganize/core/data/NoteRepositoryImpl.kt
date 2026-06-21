package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.NoteDao
import com.metoly.morganize.core.database.mapper.NoteMapper
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.asResponseState
import com.metoly.morganize.core.model.suspendAsResponseStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import android.content.Context
import android.content.Intent

/**
 * Concrete implementation of [NoteRepository] leveraging the Room [NoteDao].
 * Maps between [com.metoly.morganize.core.database.entity.NoteEntity] (persistence)
 * and [Note] (domain) via [NoteMapper].
 */
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper,
    private val context: Context
) : NoteRepository {

    private fun triggerWidgetUpdate() {
        val intent = Intent("com.metoly.morganize.ACTION_UPDATE_WIDGET").apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    override fun getAllNotes(): Flow<ResponseState<List<Note>>> =
        noteDao.getAllNotes()
            .map { entities -> noteMapper.toDomainList(entities) }
            .asResponseState()

    override fun getNotesByCategory(categoryId: Long): Flow<ResponseState<List<Note>>> =
        noteDao.getNotesByCategory(categoryId)
            .map { entities -> noteMapper.toDomainList(entities) }
            .asResponseState()

    override fun searchNotes(query: String): Flow<ResponseState<List<Note>>> =
        noteDao.searchNotes(query)
            .map { entities -> noteMapper.toDomainList(entities) }
            .asResponseState()

    override fun getNoteById(id: Long): Flow<ResponseState<Note?>> =
        noteDao.getNoteById(id)
            .map { entity -> entity?.let { noteMapper.toDomain(it) } }
            .asResponseState()

    override fun getDeletedNotes(): Flow<ResponseState<List<Note>>> =
        noteDao.getDeletedNotes()
            .map { entities -> noteMapper.toDomainList(entities) }
            .asResponseState()

    override fun getArchivedNotes(): Flow<ResponseState<List<Note>>> =
        noteDao.getArchivedNotes()
            .map { entities -> noteMapper.toDomainList(entities) }
            .asResponseState()

    override fun insertNote(note: Note): Flow<ResponseState<Long>> = suspendAsResponseStateFlow {
        noteDao.insertNote(noteMapper.toEntity(note)).also { triggerWidgetUpdate() }
    }

    override fun updateNote(note: Note): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.updateNote(noteMapper.toEntity(note)).also { triggerWidgetUpdate() }
    }

    override fun deleteNote(note: Note): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.deleteNote(noteMapper.toEntity(note)).also { triggerWidgetUpdate() }
    }

    override fun deleteNoteById(id: Long): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        noteDao.deleteNoteById(id).also { triggerWidgetUpdate() }
    }

    override fun setPinned(id: Long, isPinned: Boolean): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { noteDao.setPinned(id, isPinned).also { triggerWidgetUpdate() } }

    override fun softDelete(id: Long): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { noteDao.softDelete(id).also { triggerWidgetUpdate() } }

    override fun restoreNote(id: Long): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { noteDao.restoreNote(id).also { triggerWidgetUpdate() } }

    override fun setArchived(id: Long, isArchived: Boolean): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { noteDao.setArchived(id, isArchived).also { triggerWidgetUpdate() } }

    override fun duplicateNote(id: Long): Flow<ResponseState<Long>> = suspendAsResponseStateFlow {
        val noteEntity = noteDao.getNoteById(id).firstOrNull()
            ?: throw Exception("Note not found")
        val duplicatedEntity = noteEntity.copy(
            id = 0,
            title = "${noteEntity.title} (copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false
        )
        noteDao.insertNote(duplicatedEntity).also { triggerWidgetUpdate() }
    }
}
