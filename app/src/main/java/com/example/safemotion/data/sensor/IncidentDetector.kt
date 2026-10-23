package com.example.safemotion.data.sensor

import com.example.safemotion.data.model.IncidentType
import kotlin.math.abs
import kotlin.math.sqrt

class IncidentDetector {
    private var previousAcceleration: FloatArray? = null
    private var previousRotation: Float? = null
    private var lastMovementNs: Long? = null
    private var lastImpactNs: Long? = null
    private var lastRotationNs: Long? = null

    fun onAcceleration(x: Float, y: Float, z: Float, timeNs: Long): IncidentType? {
        val magnitude = sqrt(x * x + y * y + z * z)
        val previous = previousAcceleration
        if (previous != null && magnitude >= IMPACT_THRESHOLD &&
            sqrt(previous[0] * previous[0] + previous[1] * previous[1] +
                previous[2] * previous[2]) < IMPACT_THRESHOLD
        ) {
            lastImpactNs = timeNs
        }
        val movement = previous != null &&
            sqrt(
                (x - previous[0]) * (x - previous[0]) +
                    (y - previous[1]) * (y - previous[1]) +
                    (z - previous[2]) * (z - previous[2])
            ) >= ACCELERATION_CHANGE_THRESHOLD
        previousAcceleration = floatArrayOf(x, y, z)
        return checkIncident(timeNs, movement)
    }

    fun onRotation(x: Float, y: Float, z: Float, timeNs: Long): IncidentType? {
        val magnitude = sqrt(x * x + y * y + z * z)
        if (magnitude >= ROTATION_THRESHOLD &&
            previousRotation?.let { it < ROTATION_THRESHOLD } == true
        ) {
            lastRotationNs = timeNs
        }
        previousRotation = magnitude
        return checkIncident(timeNs, magnitude >= MOVEMENT_ROTATION_THRESHOLD)
    }

    fun reset() {
        previousAcceleration = null
        previousRotation = null
        lastMovementNs = null
        lastImpactNs = null
        lastRotationNs = null
    }

    private fun checkIncident(timeNs: Long, moving: Boolean): IncidentType? {
        val impact = lastImpactNs
        val rotation = lastRotationNs
        if (impact != null && rotation != null &&
            abs(impact - rotation) <= FALL_WINDOW_NS &&
            timeNs - maxOf(impact, rotation) <= FALL_WINDOW_NS
        ) {
            lastImpactNs = null
            lastRotationNs = null
            lastMovementNs = timeNs
            return IncidentType.FALL
        }

        if (moving) lastMovementNs = timeNs
        val lastMovement = lastMovementNs ?: timeNs.also { lastMovementNs = it }
        if (timeNs - lastMovement >= INACTIVITY_NS) {
            lastMovementNs = timeNs
            return IncidentType.INACTIVITY
        }
        return null
    }

    private companion object {
        const val IMPACT_THRESHOLD = 24f
        const val ROTATION_THRESHOLD = 2.5f
        const val MOVEMENT_ROTATION_THRESHOLD = 0.35f
        const val ACCELERATION_CHANGE_THRESHOLD = 0.7f
        const val FALL_WINDOW_NS = 1_000_000_000L
        const val INACTIVITY_NS = 60_000_000_000L
    }
}
