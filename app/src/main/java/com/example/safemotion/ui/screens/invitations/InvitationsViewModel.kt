package com.example.safemotion.ui.screens.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.repository.FirestoreGuardianRepository
import com.example.safemotion.data.repository.GuardianRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvitationsViewModel(
    private val repository: GuardianRepository = FirestoreGuardianRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InvitationsUiState())
    val uiState = _uiState.asStateFlow()

    private var invitationsJob: Job? = null
    private var ultimaRespondida: String? = null

    // Escucha en tiempo real las invitaciones pendientes del usuario actual.
    fun refresh() {
        invitationsJob?.cancel()
        invitationsJob = viewModelScope.launch {
            repository.receivedInvitations()
                .catch { }
                .collect { invitations -> _uiState.update { it.copy(invitations = invitations) } }
        }
    }

    fun respond(invitationId: String, accepted: Boolean): Boolean {
        if (_uiState.value.invitations.none { it.id == invitationId }) return false
        ultimaRespondida = invitationId
        // Se quita de inmediato; Firestore confirma el cambio en la siguiente actualización.
        _uiState.update { state ->
            state.copy(invitations = state.invitations.filter { it.id != invitationId })
        }
        viewModelScope.launch { runCatching { repository.respond(invitationId, accepted) } }
        return true
    }

    fun undoResponse() {
        val invitationId = ultimaRespondida ?: return
        ultimaRespondida = null
        viewModelScope.launch { runCatching { repository.undoResponse(invitationId) } }
    }
}
