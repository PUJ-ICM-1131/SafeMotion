package com.example.safemotion.data.repository

import com.example.safemotion.data.model.CompletedRun
import com.example.safemotion.data.model.ConfirmedIncident
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.RunGuardian
import com.example.safemotion.data.model.RunSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockRunRepository {
    val availableGuardians = listOf(
        RunGuardian(1, "Andrés Rojas"),
        RunGuardian(2, "Laura Gómez")
    )

    private val _activeRun = MutableStateFlow<RunSession?>(null)
    val activeRun = _activeRun.asStateFlow()

    private val _completedRuns = MutableStateFlow<List<CompletedRun>>(emptyList())
    val completedRuns = _completedRuns.asStateFlow()

    private val _confirmedIncidents = MutableStateFlow<List<ConfirmedIncident>>(emptyList())
    val confirmedIncidents = _confirmedIncidents.asStateFlow()

    fun startRun(activity: RunActivityType, guardianIds: Set<Int>): RunSession? {
        if (_activeRun.value != null || guardianIds.isEmpty()) return null
        val validIds = availableGuardians.map { it.id }.toSet()
        if (!validIds.containsAll(guardianIds)) return null
        return RunSession(activity, guardianIds).also { _activeRun.value = it }
    }

    fun advanceOneSecond(): RunSession? {
        val current = _activeRun.value ?: return null
        val distancePerSecond = if (current.activity == RunActivityType.RUNNING) 0.003 else 0.006
        return current.copy(
            elapsedSeconds = current.elapsedSeconds + 1,
            distanceKm = current.distanceKm + distancePerSecond
        ).also { _activeRun.value = it }
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
