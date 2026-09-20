package com.example.safemotion.ui.screens.guardians

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian

data class GuardiansUiState(
    val guardians: List<Guardian> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val selectedContactIds: Set<String> = emptySet()
)
