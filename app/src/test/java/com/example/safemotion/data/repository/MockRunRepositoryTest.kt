package com.example.safemotion.data.repository

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockRunRepositoryTest {
    @Before
    fun setUp() = MockRunRepository.clearForSignOut()

    @After
    fun tearDown() = MockRunRepository.clearForSignOut()

    @Test
    fun startRequiresAnAvailableGuardian() {
        assertNull(MockRunRepository.startRun(RunActivityType.RUNNING, emptySet()))
        assertNull(MockRunRepository.startRun(RunActivityType.RUNNING, setOf(999)))
        assertNull(MockRunRepository.activeRun.value)
    }

    @Test
    fun finishedRunIsSavedOnlyOnce() {
        val started = MockRunRepository.startRun(RunActivityType.CYCLING, setOf(1, 2))
        assertEquals(2, started?.guardianIds?.size)
        assertNull(MockRunRepository.startRun(RunActivityType.RUNNING, setOf(1)))

        repeat(10) { MockRunRepository.advanceOneSecond() }
        val finished = MockRunRepository.finishRun()

        assertEquals(10, finished?.durationSeconds)
        assertEquals(0.06, finished?.distanceKm ?: 0.0, 0.000001)
        assertEquals(2, finished?.guardianCount)
        assertNull(MockRunRepository.activeRun.value)
        assertNull(MockRunRepository.finishRun())
        assertEquals(1, MockRunRepository.completedRuns.value.size)
    }

    @Test
    fun incidentNeedsAnActiveRunAndClearsAtSignOut() {
        MockRunRepository.confirmIncident(IncidentType.FALL)
        assertTrue(MockRunRepository.confirmedIncidents.value.isEmpty())

        MockRunRepository.startRun(RunActivityType.RUNNING, setOf(1))
        MockRunRepository.confirmIncident(IncidentType.FALL)
        assertEquals(IncidentType.FALL, MockRunRepository.confirmedIncidents.value.single().type)
        MockRunRepository.finishRun()
        assertEquals(1, MockRunRepository.completedRuns.value.size)

        MockRunRepository.clearForSignOut()
        assertNull(MockRunRepository.activeRun.value)
        assertTrue(MockRunRepository.confirmedIncidents.value.isEmpty())
        assertTrue(MockRunRepository.completedRuns.value.isEmpty())
    }
}
