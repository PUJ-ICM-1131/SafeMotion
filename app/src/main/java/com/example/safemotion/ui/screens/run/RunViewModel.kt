package com.example.safemotion.ui.screens.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.MockRunRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RunViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        RunUiState(
            guardians = MockRunRepository.availableGuardians,
            session = MockRunRepository.activeRun.value
        )
    )
    val uiState = _uiState.asStateFlow()

    private var runClock: Job? = null
    private var alertClock: Job? = null

    fun selectActivity(activity: RunActivityType) {
        if (_uiState.value.phase == RunPhase.PREPARING) {
            _uiState.update { it.copy(activity = activity) }
        }
    }

    fun toggleGuardian(id: Int) {
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

    fun startRun(): Boolean {
        val state = _uiState.value
        if (state.phase != RunPhase.PREPARING) return false
        if (state.selectedGuardianIds.isEmpty()) {
            _uiState.update { it.copy(needsGuardian = true) }
            return false
        }
        val session = MockRunRepository.startRun(state.activity, state.selectedGuardianIds)
            ?: return false
        _uiState.update { it.copy(phase = RunPhase.ACTIVE, session = session, needsGuardian = false) }
        runClock?.cancel()
        runClock = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val updated = MockRunRepository.advanceOneSecond() ?: break
                _uiState.update { it.copy(session = updated) }
            }
        }
        return true
    }

    fun simulateIncident(type: IncidentType): Boolean {
        if (_uiState.value.phase != RunPhase.ACTIVE || _uiState.value.session == null) return false
        alertClock?.cancel()
        _uiState.update {
            it.copy(
                phase = RunPhase.ALERT,
                incidentType = type,
                secondsToCancel = 30,
                alertSent = false
            )
        }
        alertClock = viewModelScope.launch {
            for (remaining in 29 downTo 0) {
                delay(1_000)
                _uiState.update { it.copy(secondsToCancel = remaining) }
            }
            MockRunRepository.confirmIncident(type)
            _uiState.update { it.copy(alertSent = true) }
        }
        return true
    }

    fun cancelAlert(): Boolean {
        val state = _uiState.value
        if (state.phase != RunPhase.ALERT || state.alertSent || state.secondsToCancel == 0) {
            return false
        }
        alertClock?.cancel()
        _uiState.update {
            it.copy(phase = RunPhase.ACTIVE, incidentType = null, secondsToCancel = 30)
        }
        return true
    }

    fun returnToRun(): Boolean {
        if (_uiState.value.phase != RunPhase.ALERT || !_uiState.value.alertSent) return false
        _uiState.update {
            it.copy(phase = RunPhase.ACTIVE, incidentType = null, secondsToCancel = 30, alertSent = false)
        }
        return true
    }

    fun finishRun(): Boolean {
        if (_uiState.value.phase != RunPhase.ACTIVE) return false
        runClock?.cancel()
        alertClock?.cancel()
        MockRunRepository.finishRun() ?: return false
        _uiState.update {
            it.copy(
                phase = RunPhase.PREPARING,
                selectedGuardianIds = emptySet(),
                session = null,
                incidentType = null,
                secondsToCancel = 30,
                alertSent = false
            )
        }
        return true
    }
}
