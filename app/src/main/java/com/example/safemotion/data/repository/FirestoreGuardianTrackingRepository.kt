package com.example.safemotion.data.repository

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.ReceivedAlert
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.TrackingStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Lado del guardián del seguimiento en tiempo real.
 *
 * sessions/{id}: runnerUid, runnerName, runnerPhone, guardianUids, activity, status,
 *   startedAt, updatedAt, lastLocation (GeoPoint), distanceKm, elapsedSeconds.
 * sessions/{id}/points/{p}: location (GeoPoint), at.
 * alerts/{id}: sessionId, runnerUid, runnerName, guardianUids, type, location (GeoPoint), createdAt.
 *
 * Las consultas no usan orderBy para no necesitar índices; se ordena en el código.
 */
object FirestoreGuardianTrackingRepository : GuardianTrackingRepository {
    const val SESSIONS = "sessions"
    const val POINTS = "points"
    const val ALERTS = "alerts"

    private val db get() = Firebase.firestore
    private val currentUid get() = Firebase.auth.currentUser?.uid

    override fun monitoredSessions(): Flow<List<MonitoredSession>> {
        val uid = currentUid ?: return flowOf(emptyList())
        return db.collection(SESSIONS)
            .whereArrayContains("guardianUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .map { it.toSession(route = emptyList()) }
                    .sortedByDescending { it.updatedAtMillis ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    override fun session(sessionId: String): Flow<MonitoredSession?> {
        val sessionRef = db.collection(SESSIONS).document(sessionId)
        val sessionFlow = sessionRef.snapshots()
            .map<DocumentSnapshot, DocumentSnapshot?> { it }
            .catch { emit(null) }
        val routeFlow = sessionRef.collection(POINTS).snapshots()
            .map { snapshot ->
                snapshot.documents
                    .sortedBy { it.getTimestamp("at")?.toDate()?.time ?: Long.MAX_VALUE }
                    .mapNotNull { it.getGeoPoint("location")?.let { p -> MapPoint(p.latitude, p.longitude) } }
            }
            .catch { emit(emptyList()) }
        return combine(sessionFlow, routeFlow) { document, route ->
            document?.takeIf { it.exists() }?.toSession(route)
        }
    }

    override fun alerts(): Flow<List<ReceivedAlert>> {
        val uid = currentUid ?: return flowOf(emptyList())
        return db.collection(ALERTS)
            .whereArrayContains("guardianUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .map { it.toAlert() }
                    .sortedByDescending { it.occurredAtMillis }
            }
            .catch { emit(emptyList()) }
    }

    private fun DocumentSnapshot.toSession(route: List<MapPoint>): MonitoredSession {
        val last = getGeoPoint("lastLocation")?.let { MapPoint(it.latitude, it.longitude) }
        return MonitoredSession(
            id = id,
            runnerName = getString("runnerName").orEmpty(),
            phone = getString("runnerPhone").orEmpty(),
            activity = enumOrDefault(getString("activity"), RunActivityType.RUNNING),
            status = enumOrDefault(getString("status"), TrackingStatus.ACTIVE),
            elapsedSeconds = getLong("elapsedSeconds")?.toInt() ?: 0,
            distanceKm = getDouble("distanceKm") ?: 0.0,
            lastLocation = last,
            // Si todavía no hay trazado, al menos se muestra la última posición.
            route = route.ifEmpty { listOfNotNull(last) },
            updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time
        )
    }

    private fun DocumentSnapshot.toAlert() = ReceivedAlert(
        id = id,
        sessionId = getString("sessionId").orEmpty(),
        runnerName = getString("runnerName").orEmpty(),
        runnerPhone = getString("runnerPhone").orEmpty(),
        incidentType = enumOrDefault(getString("type"), IncidentType.FALL),
        occurredAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        location = getGeoPoint("location")?.let { MapPoint(it.latitude, it.longitude) }
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
