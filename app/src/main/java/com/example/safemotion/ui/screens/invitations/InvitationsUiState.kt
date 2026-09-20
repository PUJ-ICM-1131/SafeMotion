package com.example.safemotion.ui.screens.invitations

import com.example.safemotion.data.model.ReceivedInvitation

data class InvitationsUiState(
    val invitations: List<ReceivedInvitation> = emptyList()
)
