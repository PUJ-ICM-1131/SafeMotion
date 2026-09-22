package com.example.safemotion.data.model

enum class TrackingStatus { ACTIVE, INTERRUPTED, FINISHED }

data class TrackPoint(
    val x: Float,
    val y: Float
)

data class MonitoredSession(
    val id: String,
    val runnerName: String,
    val activity: RunActivityType,
    val status: TrackingStatus,
    val elapsedSeconds: Int,
    val distanceKm: Double,
    val location: String,
    val lastUpdateSeconds: Int,
    val route: List<TrackPoint>,
    val phone: String
)

data class ReceivedAlert(
    val id: String,
    val sessionId: String,
    val runnerName: String,
    val incidentType: IncidentType,
    val occurredAtMillis: Long,
    val receivedAtMillis: Long,
    val lastLocation: String
)
