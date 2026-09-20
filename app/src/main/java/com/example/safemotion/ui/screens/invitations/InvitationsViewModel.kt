package com.example.safemotion.ui.screens.invitations

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.model.ReceivedInvitation
import com.example.safemotion.data.repository.MockGuardianRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InvitationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        InvitationsUiState(invitations = MockGuardianRepository.receivedInvitations.value)
    )
    val uiState = _uiState.asStateFlow()

    private var ultimaRespondida: ReceivedInvitation? = null
    private var ultimaPosicion = 0

    fun refresh() {
        _uiState.update {
            it.copy(invitations = MockGuardianRepository.receivedInvitations.value)
        }
    }

    fun respond(invitationId: String): Boolean {
        val invitaciones = _uiState.value.invitations
        val posicion = invitaciones.indexOfFirst { it.id == invitationId }
        if (posicion < 0) return false
        ultimaRespondida = invitaciones[posicion]
        ultimaPosicion = posicion
        MockGuardianRepository.respond(invitationId)
        _uiState.update {
            it.copy(invitations = MockGuardianRepository.receivedInvitations.value)
        }
        return true
    }

    fun undoResponse() {
        val invitacion = ultimaRespondida ?: return
        MockGuardianRepository.restore(invitacion, ultimaPosicion)
        ultimaRespondida = null
        _uiState.update {
            it.copy(invitations = MockGuardianRepository.receivedInvitations.value)
        }
    }
}
