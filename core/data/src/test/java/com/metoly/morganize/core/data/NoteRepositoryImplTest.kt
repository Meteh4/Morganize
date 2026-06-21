package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.NoteDao
import com.metoly.morganize.core.database.entity.NoteEntity
import com.metoly.morganize.core.database.mapper.NoteMapper
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteRepositoryImplTest {

    private lateinit var noteDao: NoteDao
    private lateinit var noteMapper: NoteMapper
    private lateinit var noteRepository: NoteRepositoryImpl

    @Before
    fun setUp() {
        noteDao = mockk()
        noteMapper = mockk()
        noteRepository = NoteRepositoryImpl(noteDao, noteMapper)
    }

    @Test
    fun `getAllNotes returns mapped success state`() = runTest {
        // Arrange
        val noteEntities = listOf(
            NoteEntity(id = 1L, title = "Test Note", createdAt = 1000L, updatedAt = 1000L)
        )
        val notes = listOf(
            Note(id = 1L, title = "Test Note", createdAt = 1000L, updatedAt = 1000L)
        )
        every { noteDao.getAllNotes() } returns flowOf(noteEntities)
        every { noteMapper.toDomainList(noteEntities) } returns notes

        // Act
        val resultFlow = noteRepository.getAllNotes()
        val result = resultFlow.first { it is ResponseState.Success || it is ResponseState.Error }

        // Assert
        assertTrue(result is ResponseState.Success)
        val resultNotes = (result as ResponseState.Success).data
        assertEquals(1, resultNotes.size)
        assertEquals(1L, resultNotes[0].id)
        assertEquals("Test Note", resultNotes[0].title)
    }

    @Test
    fun `insertNote calls dao and returns id`() = runTest {
        // Arrange
        val note = Note(id = 0L, title = "New Note", createdAt = 1000L, updatedAt = 1000L)
        val noteEntity = NoteEntity(id = 0L, title = "New Note", createdAt = 1000L, updatedAt = 1000L)
        
        every { noteMapper.toEntity(note) } returns noteEntity
        coEvery { noteDao.insertNote(any()) } returns 2L

        // Act
        val resultFlow = noteRepository.insertNote(note)
        val result = resultFlow.first { it is ResponseState.Success || it is ResponseState.Error }

        // Assert
        assertTrue(result is ResponseState.Success)
        assertEquals(2L, (result as ResponseState.Success).data)
        coVerify(exactly = 1) { noteDao.insertNote(any()) }
    }
}
