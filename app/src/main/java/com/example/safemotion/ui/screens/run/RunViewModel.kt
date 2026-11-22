package com.example.safemotion.ui.screens.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RunGuardian
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.GuardianRepository
import com.example.safemotion.data.repository.MockRunRepository
import com.example.safemotion.data.repository.PositionTracker
import com.example.safemotion.data.repository.RunTrackingRepository
import com.example.safemotion.data.sensor.RunSensorMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RunViewModel(
    private val sensorMonitor: RunSensorMonitor,
    private val guardianRepository: GuardianRepository,
    private val trackingRepository: RunTrackingRepository,
    private val positionTracker: PositionTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RunUiState(
            session = MockRunRepository.activeRun.value,
            sensorStatus = sensorMonitor.status.value
        )
    )
    val uiState = _uiState.asStateFlow()

    private var guardiansJob: Job? = null
    private var runClock: Job? = null
    private var trackingJob: Job? = null
    private var alertClock: Job? = null
    private var pendingAlertId: String? = null

    // Progreso real del recorrido, calculado con el GPS.
    private var elapsedSeconds = 0L
    private var distanceMeters = 0.0
    private var lastCountedPoint: MapPoint? = null

    init {
        viewModelScope.launch {
            sensorMonitor.status.collect { status ->
                _uiState.update { it.copy(sensorStatus = status) }
            }
        }
        viewModelScope.launch {
            sensorMonitor.incidents.collect { type -> simulateIncident(type) }
        }
    }

    // Escucha en tiempo real las invitaciones del usuario y deja solo los guardianes que aceptaron.
    fun loadGuardians() {
        guardiansJob?.cancel()
        guardiansJob = viewModelScope.launch {
            guardianRepository.guardians()
                .catch { emit(emptyList()) }
                .collect { all ->
                    val accepted = all
                        .filter { it.status == InvitationStatus.ACCEPTED && it.uid.isNotBlank() }
                        .map { RunGuardian(id = it.uid, name = it.name) }
                    val acceptedIds = accepted.map { it.id }.toSet()
                    _uiState.update { state ->
                        state.copy(
                            guardians = accepted,
                            guardiansLoaded = true,
                            // Si un guardián deja de estar aceptado, se quita de la selección.
                            selectedGuardianIds = if (state.phase == RunPhase.PREPARING) {
                                state.selectedGuardianIds intersect acceptedIds
                            } else {
                                state.selectedGuardianIds
                            }
                        )
                    }
                }
        }
    }

    fun selectActivity(activity: RunActivityType) {
        if (_uiState.value.phase == RunPhase.PREPARING) {
            _uiState.update { it.copy(activity = activity) }
        }
    }

    fun toggleGuardian(id: String) {
        if (_uiState.value.phase != RunPhase.PREPARING) return
        if (_uiState.value.guardians.none { it.id == id }) return
        _uiState.update { state ->
            val selected = if (id in state.selectedGuardianIds) {
                state.selectedGuardianIds - id
            } else {
                state.selectedGuardianIds + id
            }
            state.copy(selectedGuardianIds = selected, needsGuardian = false)
        }
    }

    // La pantalla llama esto cuando ya tiene el permiso de ubicación.
    fun startRun() {
        val state = _uiState.value
        if (state.phase != RunPhase.PREPARING || state.isStarting) return
        if (state.selectedGuardianIds.isEmpty()) {
            _uiState.update { it.copy(needsGuardian = true) }
            return
        }
        _uiState.update {
            it.copy(isStarting = true, startFailed = false, locationDenied = false, needsGuardian = false)
        }
        viewModelScope.launch {
            // Primero se crea la sesión en Firestore; si falla (sin red o sin permiso), no se inicia.
            val sessionId = try {
                trackingRepository.startSession(state.activity, state.selectedGuardianIds)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val session = sessionId?.let {
                MockRunRepository.startRun(state.activity, state.selectedGuardianIds)
            }
            if (sessionId == null || session == null) {
                sessionId?.let { trackingRepository.finishSession(it, 0.0, 0L) }
                _uiState.update { it.copy(isStarting = false, startFailed = true) }
                return@launch
            }
            _uiState.update {
                it.copy(phase = RunPhase.ACTIVE, session = session, sessionId = sessionId, isStarting = false)
            }
            startTracking(sessionId)
        }
    }

    fun onLocationDenied() {
        _uiState.update { it.copy(locationDenied = true) }
    }

    private fun startTracking(sessionId: String) {
        sensorMonitor.start()
        elapsedSeconds = 0L
        distanceMeters = 0.0
        lastCountedPoint = null
        runClock?.cancel()
        runClock = viewModelScope.launch {
            while (true) {
                delay(1_000)
                elapsedSeconds++
                if (!refreshProgress()) break
            }
        }
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            positionTracker.positions()
                .catch { /* Sin permiso o sin GPS: el recorrido sigue, pero sin publicar posición. */ }
                .collect { point -> onPosition(sessionId, point) }
        }
    }

    // Con cada posición: suma la distancia, actualiza la pantalla y publica en Firestore.
    private fun onPosition(sessionId: String, point: MapPoint) {
        val previous = lastCountedPoint
        val step = previous?.let { positionTracker.distanceMeters(it, point) } ?: 0f
        // Saltos de menos de 3 m son ruido del GPS: no suman distancia ni puntos al trazado.
        val moved = previous == null || step >= MIN_STEP_METERS
        if (moved) {
            distanceMeters += step
            lastCountedPoint = point
        }
        refreshProgress(point)
        trackingRepository.publishPosition(
            sessionId = sessionId,
            point = point,
            distanceKm = distanceMeters / 1_000.0,
            elapsedSeconds = elapsedSeconds,
            addToRoute = moved
        )
    }

    private fun refreshProgress(location: MapPoint? = null): Boolean {
        val session = MockRunRepository.updateProgress(
            elapsedSeconds = elapsedSeconds.toInt(),
            distanceKm = distanceMeters / 1_000.0
        ) ?: return false
        _uiState.update { it.copy(session = session, currentLocation = location ?: it.currentLocation) }
        return true
    }

    private fun stopTracking() {
        runClock?.cancel()
        trackingJob?.cancel()
    }

    fun simulateIncident(type: IncidentType): Boolean {
        if (_uiState.value.phase != RunPhase.ACTIVE || _uiState.value.session == null) return false
        sensorMonitor.stop()
        alertClock?.cancel()
        pendingAlertId = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                phase = RunPhase.ALERT,
                incidentType = type,
                secondsToCancel = 30,
                alertSent = false,
                alertSending = false,
                alertFailed = false
            )
        }
        alertClock = viewModelScope.launch {
            for (remaining in 29 downTo 0) {
                delay(1_000)
                _uiState.update { it.copy(secondsToCancel = remaining) }
            }
            sendAlert()
        }
        return true
    }

    fun retryAlert() {
        val state = _uiState.value
        if (state.phase != RunPhase.ALERT || !state.alertFailed || state.alertSending) return
        alertClock = viewModelScope.launch { sendAlert() }
    }

    private suspend fun sendAlert() {
        val state = _uiState.value
        val id = pendingAlertId ?: return
        val sessionId = state.sessionId ?: return
        val type = state.incidentType ?: return
        _uiState.update { it.copy(alertSending = true, alertFailed = false) }
        try {
            trackingRepository.createAlert(
                id, sessionId, state.selectedGuardianIds, type, state.currentLocation
            )
            MockRunRepository.confirmIncident(type)
            _uiState.update { it.copy(alertSending = false, alertSent = true) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(alertSending = false, alertFailed = true) }
        }
    }

    fun cancelAlert(): Boolean {
        val state = _uiState.value
        if (state.phase != RunPhase.ALERT || state.alertSent || state.secondsToCancel == 0) {
            return false
        }
        alertClock?.cancel()
        pendingAlertId = null
        _uiState.update {
            it.copy(phase = RunPhase.ACTIVE, incidentType = null, secondsToCancel = 30,
                alertSending = false, alertFailed = false)
        }
        sensorMonitor.start()
        return true
    }

    fun returnToRun(): Boolean {
        if (_uiState.value.phase != RunPhase.ALERT || !_uiState.value.alertSent) return false
        _uiState.update {
            it.copy(phase = RunPhase.ACTIVE, incidentType = null, secondsToCancel = 30,
                alertSent = false, alertSending = false, alertFailed = false)
        }
        pendingAlertId = null
        sensorMonitor.start()
        return true
    }

    fun finishRun(): Boolean {
        if (_uiState.value.phase != RunPhase.ACTIVE) return false
        stopTracking()
        alertClock?.cancel()
        pendingAlertId = null
        sensorMonitor.stop()
        val finished = MockRunRepository.finishRun() ?: return false
        _uiState.value.sessionId?.let {
            trackingRepository.finishSession(it, finished.distanceKm, finished.durationSeconds.toLong())
        }
        _uiState.update {
            it.copy(
                phase = RunPhase.PREPARING,
                selectedGuardianIds = emptySet(),
                session = null,
                sessionId = null,
                currentLocation = null,
                incidentType = null,
                secondsToCancel = 30,
                alertSent = false,
                alertSending = false,
                alertFailed = false
            )
        }
        return true
    }

    override fun onCleared() {
        stopTracking()
        sensorMonitor.stop()
    }

    private companion object {
        const val MIN_STEP_METERS = 3f
    }
}
