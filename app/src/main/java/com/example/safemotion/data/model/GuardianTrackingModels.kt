package com.example.safemotion.data.model

enum class TrackingStatus { ACTIVE, INTERRUPTED, FINISHED }

// Si el corredor no publica su posición en este tiempo, la sesión se muestra como interrumpida.
const val INTERRUPTED_AFTER_SECONDS = 120

/** Recorrido de otra persona que el usuario acompaña como guardián (colección sessions). */
data class MonitoredSession(
    val id: String,
    val runnerName: String,
    val phone: String,
    val activity: RunActivityType,
    val status: TrackingStatus,
    val elapsedSeconds: Int,
    val distanceKm: Double,
    val lastLocation: MapPoint?,
    val route: List<MapPoint>,
    val updatedAtMillis: Long?
) {
    fun secondsSinceUpdate(nowMillis: Long): Int =
        updatedAtMillis?.let { ((nowMillis - it) / 1000).coerceAtLeast(0).toInt() } ?: 0

    // ACTIVE en Firestore, pero sin posiciones nuevas, se considera interrumpida.
    fun statusAt(nowMillis: Long): TrackingStatus =
        if (status == TrackingStatus.ACTIVE && secondsSinceUpdate(nowMillis) > INTERRUPTED_AFTER_SECONDS) {
            TrackingStatus.INTERRUPTED
        } else {
            status
        }
}

/** Alerta de caída o inactividad de alguien que el usuario acompaña (colección alerts). */
data class ReceivedAlert(
    val id: String,
    val sessionId: String,
    val runnerName: String,
    val runnerPhone: String,
    val incidentType: IncidentType,
    val occurredAtMillis: Long,
    val location: MapPoint?
)
