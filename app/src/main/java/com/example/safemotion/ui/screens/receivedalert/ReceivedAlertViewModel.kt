package com.example.safemotion.ui.screens.receivedalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.repository.FirestoreGuardianTrackingRepository
import com.example.safemotion.data.repository.GuardianTrackingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceivedAlertViewModel(
    private val repository: GuardianTrackingRepository = FirestoreGuardianTrackingRepository,
    private val addressFor: suspend (MapPoint) -> String? = { null }
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceivedAlertUiState())
    val uiState = _uiState.asStateFlow()

    private var alertJob: Job? = null
    private var geocodedLocation: MapPoint? = null

    /** Muestra una alerta; sin id, la más reciente de las personas que el usuario acompaña. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectAlert(alertId: String?) {
        alertJob?.cancel()
        _uiState.value = ReceivedAlertUiState()
        geocodedLocation = null
        alertJob = viewModelScope.launch {
            repository.alerts()
                .map { alerts -> if (alertId == null) alerts.firstOrNull() else alerts.find { it.id == alertId } }
                .distinctUntilChanged()
                .flatMapLatest { alert ->
                    if (alert == null) {
                        flowOf(null to null)
                    } else {
                        repository.session(alert.sessionId).map { session -> alert to session }
                    }
                }
                .collect { (alert, session) ->
                    _uiState.update {
                        it.copy(alert = alert, session = session, isLoading = false, isMissing = alert == null)
                    }
                    alert?.location?.let(::updateAddress)
                }
        }
    }

    private fun updateAddress(location: MapPoint) {
        if (location == geocodedLocation) return
        geocodedLocation = location
        viewModelScope.launch {
            val address = runCatching { addressFor(location) }.getOrNull()
            if (address != null) _uiState.update { it.copy(address = address) }
        }
    }
}
