package com.example.safemotion.ui.screens.receivedalert

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.ReceivedAlert
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.TrackingStatus
import com.example.safemotion.data.repository.GuardianTrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReceivedAlertViewModelTest {
    private class FakeTrackingRepository : GuardianTrackingRepository {
        val sessions = MutableStateFlow<List<MonitoredSession>>(emptyList())
        val alerts = MutableStateFlow<List<ReceivedAlert>>(emptyList())
        override fun monitoredSessions(): Flow<List<MonitoredSession>> = sessions
        override fun session(sessionId: String): Flow<MonitoredSession?> =
            sessions.map { list -> list.find { it.id == sessionId } }
        override fun alerts(): Flow<List<ReceivedAlert>> = alerts
    }

    private fun alert(id: String, at: Long) = ReceivedAlert(
        id = id,
        sessionId = "s1",
        runnerName = "Camilo Restrepo",
        runnerPhone = "3112458890",
        incidentType = IncidentType.FALL,
        occurredAtMillis = at,
        location = MapPoint(4.69, -74.04)
    )

    private val session = MonitoredSession(
        id = "s1",
        runnerName = "Camilo Restrepo",
        phone = "3112458890",
        activity = RunActivityType.CYCLING,
        status = TrackingStatus.ACTIVE,
        elapsedSeconds = 900,
        distanceKm = 3.2,
        lastLocation = MapPoint(4.69, -74.04),
        route = emptyList(),
        updatedAtMillis = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withoutIdShowsTheMostRecentAlertWithItsSession() {
        val repository = FakeTrackingRepository()
        repository.sessions.value = listOf(session)
        // El repositorio entrega las alertas de la más reciente a la más antigua.
        repository.alerts.value = listOf(alert("nueva", at = 2_000), alert("vieja", at = 1_000))
        val viewModel = ReceivedAlertViewModel(repository, addressFor = { "Calle 116 # 15-20" })

        viewModel.selectAlert(null)

        val state = viewModel.uiState.value
        assertEquals("nueva", state.alert?.id)
        assertEquals("s1", state.monitoredSessionId)
        assertEquals(RunActivityType.CYCLING, state.session?.activity)
        assertEquals("Calle 116 # 15-20", state.address)
    }

    @Test
    fun newAlertsAppearInRealTime() {
        val repository = FakeTrackingRepository()
        val viewModel = ReceivedAlertViewModel(repository)
        viewModel.selectAlert(null)
        assertTrue(viewModel.uiState.value.isMissing)

        repository.alerts.value = listOf(alert("a1", at = 1_000))

        assertEquals("a1", viewModel.uiState.value.alert?.id)
    }

    @Test
    fun missingAlertProducesEmptyState() {
        val viewModel = ReceivedAlertViewModel(FakeTrackingRepository())

        viewModel.selectAlert("no-existe")

        assertNull(viewModel.uiState.value.alert)
        assertTrue(viewModel.uiState.value.isMissing)
    }
}
