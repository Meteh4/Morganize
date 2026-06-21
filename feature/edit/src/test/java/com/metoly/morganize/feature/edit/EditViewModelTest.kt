package com.metoly.morganize.feature.edit

import com.metoly.components.model.NoteEditorEvent
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.security.EncryptionManager
import com.metoly.morganize.core.model.security.KeyManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var encryptionManager: EncryptionManager
    private lateinit var keyManager: KeyManager
    private lateinit var viewModel: EditViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = mockk()
        categoryRepository = mockk()
        encryptionManager = mockk(relaxed = true)
        keyManager = mockk(relaxed = true)

        val fakeCategories = listOf(
            Category(id = 1L, name = "Work", colorArgb = 0xFF0000)
        )
        val fakeNote = Note(id = 1L, title = "Original Title", createdAt = 1000L, updatedAt = 1000L)

        every { categoryRepository.getAllCategories() } returns flowOf(ResponseState.Success(fakeCategories))
        every { noteRepository.getNoteById(1L) } returns flowOf(ResponseState.Success(fakeNote))

        viewModel = EditViewModel(1L, noteRepository, categoryRepository, encryptionManager, keyManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads note and categories`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Original Title", state.title)
    }

    @Test
    fun `title change updates state via delegate`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.delegate.onEvent(NoteEditorEvent.TitleChanged("New Title"))
        assertEquals("New Title", viewModel.uiState.value.title)
    }

    @Test
    fun `save note updates repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.delegate.onEvent(NoteEditorEvent.TitleChanged("Updated Title"))
        
        coEvery { noteRepository.updateNote(any()) } returns flowOf(ResponseState.Success(Unit))

        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { noteRepository.updateNote(any()) }
    }

    @Test
    fun `delete note calls repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        coEvery { noteRepository.deleteNote(any()) } returns flowOf(ResponseState.Success(Unit))

        viewModel.confirmDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { noteRepository.deleteNote(any()) }
    }
}
