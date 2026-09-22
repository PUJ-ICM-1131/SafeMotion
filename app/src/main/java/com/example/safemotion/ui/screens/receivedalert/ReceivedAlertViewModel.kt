package com.example.safemotion.ui.screens.receivedalert

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.repository.MockGuardianTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReceivedAlertViewModel(initialAlertId: String = "alert-camilo") : ViewModel() {
    private val _uiState = MutableStateFlow(stateFor(initialAlertId))
    val uiState = _uiState.asStateFlow()

    fun selectAlert(alertId: String) {
        _uiState.value = stateFor(alertId)
    }

    private fun stateFor(alertId: String): ReceivedAlertUiState {
        val alert = MockGuardianTrackingRepository.findAlert(alertId)
        val session = MockGuardianTrackingRepository.sessionForAlert(alertId)
        return ReceivedAlertUiState(
            alert = alert,
            monitoredSessionId = session?.id,
            isMissing = alert == null
        )
    }
}
