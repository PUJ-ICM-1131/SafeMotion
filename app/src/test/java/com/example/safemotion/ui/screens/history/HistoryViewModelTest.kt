package com.example.safemotion.ui.screens.history

import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.MockRunRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HistoryViewModelTest {
    @Before
    fun setUp() = MockRunRepository.clearForSignOut()

    @After
    fun tearDown() = MockRunRepository.clearForSignOut()

    @Test
    fun noCompletedRunsProducesEmptyStateWhenSamplesAreDisabled() {
        val viewModel = HistoryViewModel(includeSamples = false)

        assertTrue(viewModel.uiState.value.runs.isEmpty())
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun refreshIncludesRunCompletedInCurrentProcess() {
        MockRunRepository.startRun(RunActivityType.CYCLING, setOf(1))
        repeat(12) { MockRunRepository.advanceOneSecond() }
        MockRunRepository.finishRun()
        val viewModel = HistoryViewModel(includeSamples = false)

        viewModel.refresh()

        val run = viewModel.uiState.value.runs.single()
        assertEquals(RunActivityType.CYCLING, run.activity)
        assertEquals(12, run.durationSeconds)
        assertEquals(HistoryRunStatus.COMPLETED, run.status)
    }

    @Test
    fun demoHistoryContainsSeveralScrollableItems() {
        val viewModel = HistoryViewModel()

        assertTrue(viewModel.uiState.value.runs.size >= 4)
    }
}
