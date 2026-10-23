package com.example.safemotion.data.sensor

import com.example.safemotion.data.model.IncidentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IncidentDetectorTest {
    @Test
    fun impactAndRotationTogetherTriggerFall() {
        val detector = IncidentDetector()
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 0L))
        assertNull(detector.onRotation(0f, 0f, 0f, 0L))
        assertNull(detector.onAcceleration(0f, 0f, 26f, 1_000_000_000L))
        assertEquals(
            IncidentType.FALL,
            detector.onRotation(0f, 3f, 0f, 1_500_000_000L)
        )
    }

    @Test
    fun impactWithoutNearbyRotationDoesNotTriggerFall() {
        val detector = IncidentDetector()
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 0L))
        assertNull(detector.onRotation(0f, 0f, 0f, 0L))
        assertNull(detector.onAcceleration(0f, 0f, 26f, 1_000_000_000L))
        assertNull(detector.onRotation(0f, 3f, 0f, 3_000_000_000L))
    }

    @Test
    fun oneMinuteWithoutMovementTriggersInactivity() {
        val detector = IncidentDetector()
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 0L))
        assertNull(detector.onRotation(0f, 0f, 0f, 0L))
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 59_000_000_000L))
        assertEquals(
            IncidentType.INACTIVITY,
            detector.onRotation(0f, 0f, 0f, 60_000_000_000L)
        )
    }

    @Test
    fun movementRestartsInactivityTimer() {
        val detector = IncidentDetector()
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 0L))
        assertNull(detector.onRotation(0f, 0.5f, 0f, 40_000_000_000L))
        assertNull(detector.onRotation(0f, 0f, 0f, 65_000_000_000L))
        assertEquals(
            IncidentType.INACTIVITY,
            detector.onRotation(0f, 0f, 0f, 100_000_000_000L)
        )
    }

    @Test
    fun resetClearsPendingImpactAndTimer() {
        val detector = IncidentDetector()
        detector.onAcceleration(0f, 0f, 9.8f, 0L)
        detector.onAcceleration(0f, 0f, 26f, 0L)
        detector.reset()
        assertNull(detector.onRotation(0f, 3f, 0f, 500_000_000L))
        assertNull(detector.onAcceleration(0f, 0f, 9.8f, 60_000_000_000L))
    }

    @Test
    fun sustainedHighValuesDoNotTriggerFallAfterRestart() {
        val detector = IncidentDetector()
        assertNull(detector.onAcceleration(0f, 0f, 26f, 0L))
        assertNull(detector.onRotation(0f, 3f, 0f, 0L))
        assertNull(detector.onAcceleration(0f, 0f, 26f, 100_000_000L))
        assertNull(detector.onRotation(0f, 3f, 0f, 100_000_000L))
    }
}
