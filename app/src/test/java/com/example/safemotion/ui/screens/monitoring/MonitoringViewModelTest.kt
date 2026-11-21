package com.example.safemotion.ui.screens.monitoring

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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonitoringViewModelTest {
    private class FakeTrackingRepository : GuardianTrackingRepository {
        val sessions = MutableStateFlow<List<MonitoredSession>>(emptyList())
        override fun monitoredSessions(): Flow<List<MonitoredSession>> = sessions
        override fun session(sessionId: String): Flow<MonitoredSession?> =
            sessions.map { list -> list.find { it.id == sessionId } }
        override fun alerts(): Flow<List<ReceivedAlert>> = MutableStateFlow(emptyList())
    }

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private var now = 1_000_000L

    private fun session(
        id: String,
        status: TrackingStatus = TrackingStatus.ACTIVE,
        last: MapPoint? = MapPoint(4.65, -74.09),
        updatedAt: Long? = now
    ) = MonitoredSession(
        id = id,
        runnerName = "Laura Gómez",
        phone = "3207761204",
        activity = RunActivityType.RUNNING,
        status = status,
        elapsedSeconds = 600,
        distanceKm = 1.8,
        lastLocation = last,
        route = listOfNotNull(last),
        updatedAtMillis = updatedAt
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withoutIdFollowsTheActiveSessionInRealTime() {
        val repository = FakeTrackingRepository()
        val viewModel = MonitoringViewModel(repository, clock = { now })
        repository.sessions.value = listOf(
            session("terminada", status = TrackingStatus.FINISHED),
            session("en-curso")
        )

        viewModel.selectSession(null)
        scope.runCurrent()
        assertEquals("en-curso", viewModel.uiState.value.session?.id)

        // El corredor se mueve: llega una posición nueva sin volver a seleccionar.
        val moved = MapPoint(4.66, -74.08)
        repository.sessions.value = listOf(session("en-curso", last = moved))
        scope.runCurrent()
        assertEquals(moved, viewModel.uiState.value.session?.lastLocation)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun noSessionsShowsTheEmptyState() {
        val viewModel = MonitoringViewModel(FakeTrackingRepository(), clock = { now })

        viewModel.selectSession(null)
        scope.runCurrent()

        assertNull(viewModel.uiState.value.session)
        assertTrue(viewModel.uiState.value.isMissing)
    }

    @Test
    fun activeSessionWithoutRecentUpdatesIsInterrupted() {
        val active = session("s1", updatedAt = now)

        assertEquals(TrackingStatus.ACTIVE, active.statusAt(now + 60_000))
        assertEquals(TrackingStatus.INTERRUPTED, active.statusAt(now + 180_000))
        assertEquals(180, active.secondsSinceUpdate(now + 180_000))
        assertEquals(
            TrackingStatus.FINISHED,
            session("s2", status = TrackingStatus.FINISHED, updatedAt = now).statusAt(now + 600_000)
        )
    }

    @Test
    fun addressIsLookedUpForTheRunnerLocation() {
        val repository = FakeTrackingRepository()
        repository.sessions.value = listOf(session("s1"))
        val viewModel = MonitoringViewModel(repository, addressFor = { "Calle 72 # 10-34" }, clock = { now })

        viewModel.selectSession("s1")
        scope.runCurrent()

        assertEquals("Calle 72 # 10-34", viewModel.uiState.value.address)
    }
}
