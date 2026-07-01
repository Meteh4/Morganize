package com.metoly.morganize.feature.list

import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.ExportImportHelper
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.feature.list.model.ListEvent
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var exportImportHelper: ExportImportHelper
    private lateinit var viewModel: ListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noteRepository = mockk()
        categoryRepository = mockk()
        exportImportHelper = mockk()
        
        val fakeNotes = listOf(
            Note(id = 1L, title = "A Note", createdAt = 1000L, updatedAt = 1000L),
            Note(id = 2L, title = "B Note", createdAt = 2000L, updatedAt = 2000L)
        )
        val fakeCategories = listOf(
            Category(id = 1L, name = "Work", colorArgb = 0xFF0000)
        )

        every { noteRepository.getAllNotes() } returns flowOf(ResponseState.Success(fakeNotes))
        every { categoryRepository.getAllCategories() } returns flowOf(ResponseState.Success(fakeCategories))

        viewModel = ListViewModel(noteRepository, categoryRepository, exportImportHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads notes and categories`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.notesState is ResponseState.Success)
        assertEquals(2, (state.notesState as ResponseState.Success).data.size)
        assertEquals(1, state.categories.size)
    }

    @Test
    fun `search query updates state and fetches notes`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        every { noteRepository.searchNotes("B Note") } returns flowOf(ResponseState.Success(emptyList()))
        
        viewModel.onEvent(ListEvent.SearchQueryChanged("B Note"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("B Note", state.searchQuery)
    }

    @Test
    fun `toggle pin calls repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { noteRepository.setPinned(any(), any()) } returns flowOf(ResponseState.Success(Unit))

        viewModel.onEvent(ListEvent.TogglePin(1L, true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { noteRepository.setPinned(1L, true) }
    }
}
