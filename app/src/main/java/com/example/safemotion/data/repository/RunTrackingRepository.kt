package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.TrackingStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/** Lado del corredor del seguimiento en tiempo real: escribe la sesión que leen los guardianes. */
interface RunTrackingRepository {
    /** Crea sessions/{id} con estado ACTIVE y devuelve el id de la sesión. */
    suspend fun startSession(activity: RunActivityType, guardianUids: Set<String>): String

    suspend fun createAlert(
        alertId: String,
        sessionId: String,
        guardianUids: Set<String>,
        type: IncidentType,
        location: MapPoint?
    )

    /**
     * Publica una posición: actualiza lastLocation, updatedAt, distancia y tiempo de la sesión y,
     * si [addToRoute], agrega un punto al trazado en sessions/{id}/points.
     */
    fun publishPosition(
        sessionId: String,
        point: MapPoint,
        distanceKm: Double,
        elapsedSeconds: Long,
        addToRoute: Boolean
    )

    /** Marca la sesión como FINISHED con el tiempo y la distancia finales. */
    fun finishSession(sessionId: String, distanceKm: Double, elapsedSeconds: Long)
}

/**
 * Sesiones del corredor en Cloud Firestore, con el esquema que lee
 * [FirestoreGuardianTrackingRepository] en el teléfono del guardián.
 */
object FirestoreRunTrackingRepository : RunTrackingRepository {
    private val db get() = Firebase.firestore
    private val sessions get() = db.collection(FirestoreGuardianTrackingRepository.SESSIONS)

    override suspend fun startSession(activity: RunActivityType, guardianUids: Set<String>): String {
        val user = Firebase.auth.currentUser ?: throw IllegalStateException("No hay una sesión iniciada")
        // El nombre y el teléfono del corredor salen de su perfil en users/{uid}.
        val profile = db.collection(FirebaseAuthRepository.USERS).document(user.uid).get().await()
        // document() sin id genera uno automático antes de escribir.
        val session = sessions.document()
        session.set(
            mapOf(
                "runnerUid" to user.uid,
                "runnerName" to (profile.getString("name") ?: user.displayName.orEmpty()),
                "runnerPhone" to profile.getString("phone").orEmpty(),
                "guardianUids" to guardianUids.toList(),
                "activity" to activity.name,
                "status" to TrackingStatus.ACTIVE.name,
                "startedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "distanceKm" to 0.0,
                "elapsedSeconds" to 0L
            )
        ).await()
        return session.id
    }

    override suspend fun createAlert(
        alertId: String,
        sessionId: String,
        guardianUids: Set<String>,
        type: IncidentType,
        location: MapPoint?
    ) {
        val user = Firebase.auth.currentUser ?: throw IllegalStateException("No hay una sesión iniciada")
        val session = sessions.document(sessionId).get().await()
        check(session.exists() && session.getString("runnerUid") == user.uid) {
            "La sesión no pertenece al corredor"
        }
        val savedGuardians = (session.get("guardianUids") as? List<*>)
            ?.filterIsInstance<String>()?.toSet().orEmpty()
        val recipients = guardianUids intersect savedGuardians
        check(recipients.isNotEmpty()) { "La sesión no tiene guardianes aceptados" }
        val lastLocation = location?.let { GeoPoint(it.latitude, it.longitude) }
            ?: session.getGeoPoint("lastLocation")
        val alert = mutableMapOf<String, Any>(
            "sessionId" to sessionId,
            "runnerUid" to user.uid,
            "runnerName" to session.getString("runnerName").orEmpty(),
            "runnerPhone" to session.getString("runnerPhone").orEmpty(),
            "guardianUids" to recipients.toList(),
            "type" to type.name,
            "createdAt" to FieldValue.serverTimestamp()
        )
        lastLocation?.let { alert["location"] = it }
        db.collection(FirestoreGuardianTrackingRepository.ALERTS).document(alertId).set(alert).await()
    }

    override fun publishPosition(
        sessionId: String,
        point: MapPoint,
        distanceKm: Double,
        elapsedSeconds: Long,
        addToRoute: Boolean
    ) {
        val session = sessions.document(sessionId)
        val location = GeoPoint(point.latitude, point.longitude)
        // Un batch envía las dos escrituras juntas: o se guardan ambas o ninguna.
        val batch = db.batch()
        batch.update(
            session,
            mapOf(
                "lastLocation" to location,
                "updatedAt" to FieldValue.serverTimestamp(),
                "distanceKm" to distanceKm,
                "elapsedSeconds" to elapsedSeconds
            )
        )
        if (addToRoute) {
            batch.set(
                session.collection(FirestoreGuardianTrackingRepository.POINTS).document(),
                mapOf("location" to location, "at" to FieldValue.serverTimestamp())
            )
        }
        batch.commit()
    }

    override fun finishSession(sessionId: String, distanceKm: Double, elapsedSeconds: Long) {
        // No se espera la respuesta: Firestore guarda el cambio en el teléfono y lo envía cuando hay red.
        sessions.document(sessionId).update(
            mapOf(
                "status" to TrackingStatus.FINISHED.name,
                "updatedAt" to FieldValue.serverTimestamp(),
                "distanceKm" to distanceKm,
                "elapsedSeconds" to elapsedSeconds
            )
        )
    }
}
