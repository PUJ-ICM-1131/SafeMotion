package com.example.safemotion.data.repository

import com.example.safemotion.data.model.CompletedRun
import com.example.safemotion.data.model.ConfirmedIncident
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.RunSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockRunRepository {

    private val _activeRun = MutableStateFlow<RunSession?>(null)
    val activeRun = _activeRun.asStateFlow()

    private val _completedRuns = MutableStateFlow<List<CompletedRun>>(emptyList())
    val completedRuns = _completedRuns.asStateFlow()

    private val _confirmedIncidents = MutableStateFlow<List<ConfirmedIncident>>(emptyList())
    val confirmedIncidents = _confirmedIncidents.asStateFlow()

    // Los guardianes ya vienen validados desde Firestore (invitaciones aceptadas).
    fun startRun(activity: RunActivityType, guardianIds: Set<String>): RunSession? {
        if (_activeRun.value != null || guardianIds.isEmpty()) return null
        return RunSession(activity, guardianIds).also { _activeRun.value = it }
    }
    // El tiempo y la distancia los calcula RunViewModel con el GPS; aquí solo se guardan.
    fun updateProgress(elapsedSeconds: Int, distanceKm: Double): RunSession? {
        val current = _activeRun.value ?: return null
        return current.copy(elapsedSeconds = elapsedSeconds, distanceKm = distanceKm)
            .also { _activeRun.value = it }
    }

    fun finishRun(): CompletedRun? {
        val current = _activeRun.value ?: return null
        val completed = CompletedRun(
            activity = current.activity,
            durationSeconds = current.elapsedSeconds,
            distanceKm = current.distanceKm,
            guardianCount = current.guardianIds.size
        )
        _completedRuns.value = listOf(completed) + _completedRuns.value
        _activeRun.value = null
        return completed
    }

    fun confirmIncident(type: IncidentType) {
        if (_activeRun.value == null) return
        _confirmedIncidents.value = listOf(
            ConfirmedIncident(
                type = type,
                timeMillis = System.currentTimeMillis(),
                lastLocation = "Ubicación simulada: Bogotá"
            )
        ) + _confirmedIncidents.value
    }

    fun clearForSignOut() {
        _activeRun.value = null
        _completedRuns.value = emptyList()
        _confirmedIncidents.value = emptyList()
    }
}
