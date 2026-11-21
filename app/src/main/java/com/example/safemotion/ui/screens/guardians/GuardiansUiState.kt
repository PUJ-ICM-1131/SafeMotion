package com.example.safemotion.ui.screens.guardians

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.InviteResult

data class GuardiansUiState(
    val guardians: List<Guardian> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val selectedContactIds: Set<String> = emptySet(),
    val isLoadingContacts: Boolean = false,
    val contactsPermissionDenied: Boolean = false,
    val isSending: Boolean = false,
    val inviteResult: InviteResult? = null,
    val inviteFailed: Boolean = false
)
