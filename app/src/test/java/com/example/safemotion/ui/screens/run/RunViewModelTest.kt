package com.example.safemotion.ui.screens.run

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.MockRunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        MockRunRepository.clearForSignOut()
    }

    @After
    fun tearDown() {
        MockRunRepository.clearForSignOut()
        Dispatchers.resetMain()
    }

    @Test
    fun guardianIsRequiredAndRunCanBeFinished() = runTest {
        val viewModel = RunViewModel()
        assertFalse(viewModel.startRun())
        assertTrue(viewModel.uiState.value.needsGuardian)

        viewModel.toggleGuardian(999)
        assertTrue(viewModel.uiState.value.selectedGuardianIds.isEmpty())
        viewModel.toggleGuardian(1)
        viewModel.selectActivity(RunActivityType.CYCLING)
        assertTrue(viewModel.startRun())
        assertFalse(viewModel.startRun())
        runCurrent()
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(2, viewModel.uiState.value.session?.elapsedSeconds)
        assertTrue(viewModel.finishRun())
        assertEquals(RunPhase.PREPARING, viewModel.uiState.value.phase)
        assertEquals(1, MockRunRepository.completedRuns.value.size)
    }

    @Test
    fun cancellingAlertDoesNotNotifyGuardians() = runTest {
        val viewModel = RunViewModel()
        viewModel.toggleGuardian(1)
        assertTrue(viewModel.startRun())
        runCurrent()
        assertTrue(viewModel.simulateIncident(IncidentType.FALL))
        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(25, viewModel.uiState.value.secondsToCancel)
        assertFalse(viewModel.finishRun())

        assertTrue(viewModel.cancelAlert())
        advanceTimeBy(30_000)
        runCurrent()
        assertTrue(MockRunRepository.confirmedIncidents.value.isEmpty())
        assertEquals(RunPhase.ACTIVE, viewModel.uiState.value.phase)
        assertTrue(viewModel.finishRun())
    }

    @Test
    fun alertIsConfirmedAfterThirtySecondsAndCannotBeCancelled() = runTest {
        val viewModel = RunViewModel()
        viewModel.toggleGuardian(1)
        assertTrue(viewModel.startRun())
        runCurrent()
        assertTrue(viewModel.simulateIncident(IncidentType.INACTIVITY))
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()

        assertEquals(0, viewModel.uiState.value.secondsToCancel)
        assertTrue(viewModel.uiState.value.alertSent)
        assertFalse(viewModel.cancelAlert())
        assertFalse(viewModel.finishRun())
        assertEquals(IncidentType.INACTIVITY, MockRunRepository.confirmedIncidents.value.single().type)
        assertTrue(viewModel.returnToRun())
        assertTrue(viewModel.finishRun())
    }
}
