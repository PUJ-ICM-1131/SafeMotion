package com.example.safemotion.ui.screens.monitoring

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.repository.MockGuardianTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MonitoringViewModel(initialSessionId: String = "session-laura") : ViewModel() {
    private val _uiState = MutableStateFlow(stateFor(initialSessionId))
    val uiState = _uiState.asStateFlow()

    fun selectSession(sessionId: String) {
        _uiState.value = stateFor(sessionId)
    }

    private fun stateFor(sessionId: String): MonitoringUiState {
        val session = MockGuardianTrackingRepository.findSession(sessionId)
        return MonitoringUiState(session = session, isMissing = session == null)
    }
}
