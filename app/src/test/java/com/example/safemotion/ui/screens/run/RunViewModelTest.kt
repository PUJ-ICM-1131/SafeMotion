package com.example.safemotion.ui.screens.run

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.InviteResult
import com.example.safemotion.data.model.ReceivedInvitation
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.GuardianRepository
import com.example.safemotion.data.repository.MockRunRepository
import com.example.safemotion.data.repository.RunTrackingRepository
import com.example.safemotion.data.sensor.RunSensorMonitor
import com.example.safemotion.data.sensor.SensorStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    private val guardians = FakeGuardianRepository()
    private val tracking = FakeRunTrackingRepository()

    // Crea el ViewModel y espera a que lleguen los guardianes del repositorio falso.
    private fun TestScope.newViewModel(
        monitor: FakeSensorMonitor = FakeSensorMonitor()
    ): RunViewModel {
        val viewModel = RunViewModel(monitor, guardians, tracking)
        viewModel.loadGuardians()
        runCurrent()
        return viewModel
    }

    @Test
    fun onlyAcceptedGuardiansCanBeSelected() = runTest {
        val viewModel = newViewModel()

        assertEquals(listOf("uid-laura"), viewModel.uiState.value.guardians.map { it.id })
        viewModel.toggleGuardian("uid-andres")
        assertTrue(viewModel.uiState.value.selectedGuardianIds.isEmpty())

        viewModel.toggleGuardian("uid-laura")
        assertEquals(setOf("uid-laura"), viewModel.uiState.value.selectedGuardianIds)

        // Si deja de estar aceptado, sale de la lista y de la selección.
        guardians.guardians.value = emptyList()
        runCurrent()
        assertTrue(viewModel.uiState.value.guardiansLoaded)
        assertTrue(viewModel.uiState.value.guardians.isEmpty())
        assertTrue(viewModel.uiState.value.selectedGuardianIds.isEmpty())
    }

    @Test
    fun guardianIsRequiredAndRunCanBeFinished() = runTest {
        val viewModel = newViewModel()
        viewModel.startRun()
        assertTrue(viewModel.uiState.value.needsGuardian)

        viewModel.toggleGuardian("uid-desconocido")
        assertTrue(viewModel.uiState.value.selectedGuardianIds.isEmpty())
        viewModel.toggleGuardian("uid-laura")
        viewModel.selectActivity(RunActivityType.CYCLING)
        viewModel.startRun()
        viewModel.startRun()
        runCurrent()

        // Se crea una sola sesión en Firestore y su id queda en el estado.
        assertEquals(listOf(RunActivityType.CYCLING to setOf("uid-laura")), tracking.started)
        assertEquals("session-1", viewModel.uiState.value.sessionId)
        assertEquals(RunPhase.ACTIVE, viewModel.uiState.value.phase)

        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(2, viewModel.uiState.value.session?.elapsedSeconds)
        assertTrue(viewModel.finishRun())
        assertEquals(listOf("session-1"), tracking.finished)
        assertEquals(RunPhase.PREPARING, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.sessionId)
        assertEquals(1, MockRunRepository.completedRuns.value.size)
    }

    @Test
    fun runDoesNotStartWhenTheSessionCannotBeCreated() = runTest {
        val viewModel = newViewModel()
        viewModel.toggleGuardian("uid-laura")
        tracking.failOnStart = true

        viewModel.startRun()
        runCurrent()

        assertEquals(RunPhase.PREPARING, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.startFailed)
        assertFalse(viewModel.uiState.value.isStarting)
        assertNull(MockRunRepository.activeRun.value)
    }

    @Test
    fun cancellingAlertDoesNotNotifyGuardians() = runTest {
        val viewModel = newViewModel()
        viewModel.toggleGuardian("uid-laura")
        viewModel.startRun()
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
        val viewModel = newViewModel()
        viewModel.toggleGuardian("uid-laura")
        viewModel.startRun()
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

    @Test
    fun sensorIncidentOpensAlertAndMonitoringResumesAfterCancellation() = runTest {
        val monitor = FakeSensorMonitor()
        val viewModel = newViewModel(monitor)
        viewModel.toggleGuardian("uid-laura")
        viewModel.startRun()
        runCurrent()
        assertTrue(monitor.status.value.monitoring)

        monitor.incidents.tryEmit(IncidentType.FALL)
        runCurrent()
        assertEquals(RunPhase.ALERT, viewModel.uiState.value.phase)
        assertEquals(IncidentType.FALL, viewModel.uiState.value.incidentType)
        assertFalse(monitor.status.value.monitoring)

        assertTrue(viewModel.cancelAlert())
        assertTrue(monitor.status.value.monitoring)
        assertTrue(viewModel.finishRun())
        assertFalse(monitor.status.value.monitoring)
    }

    private class FakeGuardianRepository : GuardianRepository {
        val guardians = MutableStateFlow(
            listOf(
                Guardian("inv-1", "Laura Gómez", "320", InvitationStatus.ACCEPTED, uid = "uid-laura"),
                Guardian("inv-2", "Andrés Rojas", "310", InvitationStatus.PENDING, uid = "uid-andres")
            )
        )

        override fun guardians(): Flow<List<Guardian>> = guardians
        override fun receivedInvitations(): Flow<List<ReceivedInvitation>> = MutableStateFlow(emptyList())
        override suspend fun invite(contacts: List<Contact>) = InviteResult(0, 0, emptyList())
        override suspend fun respond(invitationId: String, accepted: Boolean) = Unit
        override suspend fun undoResponse(invitationId: String) = Unit
    }

    private class FakeRunTrackingRepository : RunTrackingRepository {
        val started = mutableListOf<Pair<RunActivityType, Set<String>>>()
        val finished = mutableListOf<String>()
        var failOnStart = false

        override suspend fun startSession(activity: RunActivityType, guardianUids: Set<String>): String {
            if (failOnStart) throw IllegalStateException("Sin red")
            started += activity to guardianUids
            return "session-${started.size}"
        }

        override fun finishSession(sessionId: String, distanceKm: Double, elapsedSeconds: Long) {
            finished += sessionId
        }
    }

    private class FakeSensorMonitor : RunSensorMonitor {
        override val status = MutableStateFlow(
            SensorStatus(accelerometerAvailable = true, gyroscopeAvailable = true)
        )
        override val incidents = MutableSharedFlow<IncidentType>(extraBufferCapacity = 1)

        override fun start() {
            status.value = status.value.copy(monitoring = true)
        }

        override fun stop() {
            status.value = status.value.copy(monitoring = false)
        }
    }
}