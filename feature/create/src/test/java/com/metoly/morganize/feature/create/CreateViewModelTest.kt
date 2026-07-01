package com.metoly.morganize.feature.create

import androidx.lifecycle.SavedStateHandle
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
class CreateViewModelTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var encryptionManager: EncryptionManager
    private lateinit var keyManager: KeyManager
    private lateinit var viewModel: CreateViewModel
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
        every { categoryRepository.getAllCategories() } returns flowOf(ResponseState.Success(fakeCategories))

        viewModel = CreateViewModel(noteRepository, categoryRepository, encryptionManager, keyManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `title change updates state via delegate`() = runTest {
        viewModel.delegate.onEvent(NoteEditorEvent.TitleChanged("New Title"))
        assertEquals("New Title", viewModel.uiState.value.title)
    }

    @Test
    fun `save note calls repository`() = runTest {
        viewModel.delegate.onEvent(NoteEditorEvent.TitleChanged("New Title"))
        
        coEvery { noteRepository.insertNote(any()) } returns flowOf(ResponseState.Success(1L))

        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { noteRepository.insertNote(any()) }
    }
}
