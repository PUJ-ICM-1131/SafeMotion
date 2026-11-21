package com.example.safemotion.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.repository.FirebaseAuthRepository
import com.example.safemotion.data.repository.FirestoreGuardianRepository
import com.example.safemotion.data.repository.GuardianRepository
import com.example.safemotion.data.repository.FirestoreGuardianTrackingRepository
import com.example.safemotion.data.repository.GuardianTrackingRepository
import com.example.safemotion.data.repository.MockRunRepository
import com.example.safemotion.data.model.TrackingStatus
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val guardianRepository: GuardianRepository = FirestoreGuardianRepository,
    private val trackingRepository: GuardianTrackingRepository = FirestoreGuardianTrackingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(firstName = FirebaseAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana")
    )
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        val lastDistance = MockRunRepository.completedRuns.value.firstOrNull()?.distanceKm
        _uiState.value = HomeUiState(
            firstName = FirebaseAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana",
            lastDistanceKm = lastDistance?.let {
                String.format(Locale.getDefault(), "%.1f", it)
            } ?: "6,4",
            monitoredSessions = _uiState.value.monitoredSessions,
            receivedAlerts = _uiState.value.receivedAlerts,
            acceptedGuardians = _uiState.value.acceptedGuardians
        )
        // Recorridos en curso y alertas de las personas que el usuario acompaña.
        viewModelScope.launch {
            runCatching { trackingRepository.monitoredSessions().first() }.getOrNull()?.let { sessions ->
                _uiState.update { state ->
                    state.copy(monitoredSessions = sessions.count { it.status == TrackingStatus.ACTIVE })
                }
            }
        }
        viewModelScope.launch {
            runCatching { trackingRepository.alerts().first() }.getOrNull()?.let { alerts ->
                _uiState.update { it.copy(receivedAlerts = alerts.size) }
            }
        }
        // Guardianes que ya aceptaron, leídos de Firestore.
        viewModelScope.launch {
            runCatching { guardianRepository.guardians().first() }.getOrNull()?.let { guardians ->
                _uiState.update { state ->
                    state.copy(acceptedGuardians = guardians.count { it.status == InvitationStatus.ACCEPTED })
                }
            }
        }
    }
}
