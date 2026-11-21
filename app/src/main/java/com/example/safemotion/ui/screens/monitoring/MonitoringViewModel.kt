package com.example.safemotion.ui.screens.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.TrackingStatus
import com.example.safemotion.data.repository.FirestoreGuardianTrackingRepository
import com.example.safemotion.data.repository.GuardianTrackingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitoringViewModel(
    private val repository: GuardianTrackingRepository = FirestoreGuardianTrackingRepository,
    private val addressFor: suspend (MapPoint) -> String? = { null },
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val _uiState = MutableStateFlow(MonitoringUiState(nowMillis = clock()))
    val uiState = _uiState.asStateFlow()

    private var sessionJob: Job? = null
    private var clockJob: Job? = null
    private var geocodedLocation: MapPoint? = null
    private var geocodedAtMillis = 0L

    /**
     * Escucha en tiempo real una sesión. Sin id, sigue la sesión activa más reciente
     * de las personas que el usuario acompaña.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectSession(sessionId: String?) {
        sessionJob?.cancel()
        _uiState.update { MonitoringUiState(nowMillis = clock()) }
        geocodedLocation = null
        val sessionIds = if (sessionId != null) {
            flowOf(sessionId)
        } else {
            repository.monitoredSessions()
                .map { sessions -> pickSession(sessions)?.id }
                .distinctUntilChanged()
        }
        sessionJob = viewModelScope.launch {
            sessionIds
                .flatMapLatest { id -> if (id == null) flowOf(null) else repository.session(id) }
                .collect { session ->
                    _uiState.update {
                        it.copy(session = session, isLoading = false, isMissing = session == null)
                    }
                    session?.lastLocation?.let(::updateAddress)
                }
        }
        startClock()
    }

    // Prefiere una sesión en curso; si no hay, muestra la última que terminó.
    private fun pickSession(sessions: List<MonitoredSession>): MonitoredSession? =
        sessions.firstOrNull { it.status == TrackingStatus.ACTIVE } ?: sessions.firstOrNull()

    // Actualiza "hace X segundos" y detecta si la sesión se interrumpió.
    private fun startClock() {
        if (clockJob?.isActive == true) return
        clockJob = viewModelScope.launch {
            while (true) {
                delay(CLOCK_TICK_MILLIS)
                _uiState.update { it.copy(nowMillis = clock()) }
            }
        }
    }

    // El Geocoder se consulta como máximo cada 30 s para no saturarlo mientras el corredor se mueve.
    private fun updateAddress(location: MapPoint) {
        val now = clock()
        if (location == geocodedLocation) return
        if (geocodedLocation != null && now - geocodedAtMillis < GEOCODE_INTERVAL_MILLIS) return
        geocodedLocation = location
        geocodedAtMillis = now
        viewModelScope.launch {
            val address = runCatching { addressFor(location) }.getOrNull()
            if (address != null) _uiState.update { it.copy(address = address) }
        }
    }

    private companion object {
        const val CLOCK_TICK_MILLIS = 5_000L
        const val GEOCODE_INTERVAL_MILLIS = 30_000L
    }
}
