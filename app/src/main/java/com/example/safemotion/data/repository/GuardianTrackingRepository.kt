package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.ReceivedAlert
import kotlinx.coroutines.flow.Flow

interface GuardianTrackingRepository {
    /** Recorridos en los que el usuario actual es guardián, sin el trazado, en tiempo real. */
    fun monitoredSessions(): Flow<List<MonitoredSession>>

    /** Un recorrido con su trazado completo, en tiempo real. Emite null si no existe. */
    fun session(sessionId: String): Flow<MonitoredSession?>

    /** Alertas de las personas que el usuario acompaña, de la más reciente a la más antigua. */
    fun alerts(): Flow<List<ReceivedAlert>>
}
