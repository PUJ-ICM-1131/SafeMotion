package com.example.safemotion.data.repository

import com.example.safemotion.data.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MockGuardianTrackingRepositoryTest {
    @Test
    fun receivedAlertResolvesItsMonitoredSession() {
        val alert = MockGuardianTrackingRepository.findAlert("alert-camilo")
        val session = MockGuardianTrackingRepository.sessionForAlert("alert-camilo")

        assertEquals("Camilo Restrepo", alert?.runnerName)
        assertEquals("session-camilo", alert?.sessionId)
        assertEquals(TrackingStatus.INTERRUPTED, session?.status)
        assertSame(session, MockGuardianTrackingRepository.findSession("session-camilo"))
    }

    @Test
    fun unknownIdentifiersReturnNoData() {
        assertNull(MockGuardianTrackingRepository.findSession("missing"))
        assertNull(MockGuardianTrackingRepository.findAlert("missing"))
        assertNull(MockGuardianTrackingRepository.sessionForAlert("missing"))
    }

    @Test
    fun monitoredSessionsContainAUsefulSimulatedRoute() {
        assertTrue(MockGuardianTrackingRepository.sessions.isNotEmpty())
        assertTrue(
            MockGuardianTrackingRepository.sessions.all { session ->
                session.route.size >= 3 && session.location.isNotBlank()
            }
        )
    }
}
