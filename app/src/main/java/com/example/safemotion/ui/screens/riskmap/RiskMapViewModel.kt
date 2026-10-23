package com.example.safemotion.ui.screens.riskmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.repository.LocationRepository
import com.example.safemotion.data.repository.MockRiskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RiskMapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        RiskMapUiState(
            zones = MockRiskRepository.zones.value,
            confirmedZoneIds = MockRiskRepository.confirmedZoneIds.value
        )
    )
    val uiState = _uiState.asStateFlow()

    // Si el usuario acaba de reportar una zona, se abre su detalle en el mapa.
    fun refresh() {
        val newReportId = MockRiskRepository.consumeLastReportedZoneId()
        _uiState.update {
            it.copy(
                zones = MockRiskRepository.zones.value,
                confirmedZoneIds = MockRiskRepository.confirmedZoneIds.value,
                selectedZoneId = newReportId ?: it.selectedZoneId
            )
        }
    }

    fun selectZone(zoneId: String) {
        _uiState.update { state ->
            if (state.zones.none { it.id == zoneId }) {
                state
            } else {
                state.copy(selectedZoneId = if (state.selectedZoneId == zoneId) null else zoneId)
            }
        }
    }

    fun dismissZone() {
        _uiState.update { it.copy(selectedZoneId = null) }
    }

    fun confirmZone(zoneId: String) {
        MockRiskRepository.confirm(zoneId)
        _uiState.update {
            it.copy(
                zones = MockRiskRepository.zones.value,
                confirmedZoneIds = MockRiskRepository.confirmedZoneIds.value
            )
        }
    }

    // Se llama cuando el usuario ya dio el permiso de ubicación.
    fun loadUserLocation(repository: LocationRepository) {
        if (_uiState.value.isLocating) return
        _uiState.update { it.copy(isLocating = true, locationUnavailable = false) }
        viewModelScope.launch {
            val location = repository.currentLocation()
            _uiState.update {
                it.copy(
                    userLocation = location,
                    isLocating = false,
                    locationUnavailable = location == null,
                    locationUpdates = if (location != null) it.locationUpdates + 1 else it.locationUpdates
                )
            }
        }
    }

    // Se llama si el usuario negó el permiso de ubicación.
    fun onLocationDenied() {
        _uiState.update { it.copy(isLocating = false, locationUnavailable = true) }
    }
}