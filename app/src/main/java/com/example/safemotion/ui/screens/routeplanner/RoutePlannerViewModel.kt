package com.example.safemotion.ui.screens.routeplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RouteProfile
import com.example.safemotion.data.repository.RouteException
import com.example.safemotion.data.repository.RouteFailure
import com.example.safemotion.data.repository.RouteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutePlannerViewModel(
    private val routeRepository: RouteRepository,
    private val currentLocation: suspend () -> MapPoint?
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutePlannerUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var routeJob: Job? = null

    fun selectProfile(profile: RouteProfile) {
        if (_uiState.value.profile == profile) return
        routeJob?.cancel()
        _uiState.update {
            it.copy(profile = profile, route = null, isCalculating = false, error = null)
        }
    }

    fun loadCurrentLocation() {
        if (_uiState.value.isLocating) return
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, error = null) }
            runCatching { currentLocation() }
                .onSuccess { coordinate ->
                    if (coordinate == null) {
                        _uiState.update {
                            it.copy(
                                isLocating = false,
                                error = RoutePlannerError.LOCATION_UNAVAILABLE
                            )
                        }
                    } else {
                        routeJob?.cancel()
                        _uiState.update {
                            it.copy(
                                origin = coordinate,
                                route = null,
                                isLocating = false,
                                error = null
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLocating = false,
                            error = RoutePlannerError.LOCATION_UNAVAILABLE
                        )
                    }
                }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(isLocating = false, error = RoutePlannerError.LOCATION_PERMISSION_DENIED)
        }
    }

    fun selectDestination(destination: MapPoint) {
        routeJob?.cancel()
        _uiState.update {
            it.copy(
                destination = destination,
                route = null,
                isCalculating = false,
                error = null
            )
        }
    }

    fun calculateRoute() {
        val state = _uiState.value
        val origin = state.origin
        if (origin == null) {
            _uiState.update { it.copy(error = RoutePlannerError.ORIGIN_REQUIRED) }
            return
        }
        val destination = state.destination
        if (destination == null) {
            _uiState.update { it.copy(error = RoutePlannerError.DESTINATION_REQUIRED) }
            return
        }
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, route = null, error = null) }
            routeRepository.calculateRoute(origin, destination, state.profile)
                .onSuccess { route ->
                    _uiState.update {
                        it.copy(route = route, isCalculating = false, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(route = null, isCalculating = false, error = error.toPlannerError())
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        locationJob?.cancel()
        routeJob?.cancel()
    }
}

private fun Throwable.toPlannerError(): RoutePlannerError = when (this) {
    is RouteException -> when (failure) {
        RouteFailure.NETWORK -> RoutePlannerError.NETWORK
        RouteFailure.NO_ROUTE -> RoutePlannerError.NO_ROUTE
        RouteFailure.INVALID_RESPONSE -> RoutePlannerError.INVALID_RESPONSE
    }
    else -> RoutePlannerError.NETWORK
}
