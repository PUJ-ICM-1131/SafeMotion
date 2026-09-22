package com.example.safemotion.data.repository

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.ReceivedAlert
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.TrackPoint
import com.example.safemotion.data.model.TrackingStatus

object MockGuardianTrackingRepository {
    private val referenceTimeMillis = System.currentTimeMillis()

    val sessions = listOf(
        MonitoredSession(
            id = "session-camilo",
            runnerName = "Camilo Restrepo",
            activity = RunActivityType.RUNNING,
            status = TrackingStatus.INTERRUPTED,
            elapsedSeconds = 22 * 60 + 4,
            distanceKm = 3.9,
            location = "Calle 116 con Carrera 15, cerca del Parque El Virrey",
            lastUpdateSeconds = 4 * 60,
            route = listOf(
                TrackPoint(0.12f, 0.78f),
                TrackPoint(0.30f, 0.62f),
                TrackPoint(0.46f, 0.67f),
                TrackPoint(0.61f, 0.43f),
                TrackPoint(0.82f, 0.31f)
            ),
            phone = "300 456 7821"
        ),
        MonitoredSession(
            id = "session-laura",
            runnerName = "Laura Guzmán",
            activity = RunActivityType.CYCLING,
            status = TrackingStatus.ACTIVE,
            elapsedSeconds = 24 * 60 + 18,
            distanceKm = 4.2,
            location = "Parque El Virrey",
            lastUpdateSeconds = 8,
            route = listOf(
                TrackPoint(0.10f, 0.72f),
                TrackPoint(0.26f, 0.55f),
                TrackPoint(0.44f, 0.58f),
                TrackPoint(0.62f, 0.37f),
                TrackPoint(0.86f, 0.25f)
            ),
            phone = "320 776 1204"
        )
    )

    val alerts = listOf(
        ReceivedAlert(
            id = "alert-camilo",
            sessionId = "session-camilo",
            runnerName = "Camilo Restrepo",
            incidentType = IncidentType.FALL,
            occurredAtMillis = referenceTimeMillis - 60_000,
            receivedAtMillis = referenceTimeMillis,
            lastLocation = "Calle 116 con Carrera 15, cerca del Parque El Virrey"
        )
    )

    fun findSession(id: String): MonitoredSession? = sessions.find { it.id == id }

    fun findAlert(id: String): ReceivedAlert? = alerts.find { it.id == id }

    fun sessionForAlert(alertId: String): MonitoredSession? {
        val alert = findAlert(alertId) ?: return null
        return findSession(alert.sessionId)
    }
}
