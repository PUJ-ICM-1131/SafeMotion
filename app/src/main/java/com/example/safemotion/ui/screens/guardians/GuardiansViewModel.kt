package com.example.safemotion.ui.screens.guardians

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.repository.MockGuardianRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GuardiansViewModel : ViewModel() {
    private val todosLosContactos = MockGuardianRepository.deviceContacts

    private val _uiState = MutableStateFlow(
        GuardiansUiState(
            guardians = MockGuardianRepository.guardians.value,
            contacts = todosLosContactos
        )
    )
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update { it.copy(guardians = MockGuardianRepository.guardians.value) }
    }

    fun changeSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, contacts = buscar(query)) }
    }

    fun toggleContact(contactId: String) {
        if (todosLosContactos.none { it.id == contactId }) return
        _uiState.update { state ->
            val selected = if (contactId in state.selectedContactIds) {
                state.selectedContactIds - contactId
            } else {
                state.selectedContactIds + contactId
            }
            state.copy(selectedContactIds = selected)
        }
    }

    fun sendInvitations(): Boolean {
        val state = _uiState.value
        if (state.selectedContactIds.isEmpty()) return false
        val elegidos = todosLosContactos.filter { it.id in state.selectedContactIds }
        MockGuardianRepository.invite(elegidos)
        _uiState.update {
            it.copy(
                guardians = MockGuardianRepository.guardians.value,
                contacts = todosLosContactos,
                searchQuery = "",
                selectedContactIds = emptySet()
            )
        }
        return true
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                contacts = todosLosContactos,
                searchQuery = "",
                selectedContactIds = emptySet()
            )
        }
    }

    private fun buscar(query: String): List<Contact> {
        if (query.isBlank()) return todosLosContactos
        return todosLosContactos.filter { it.name.contains(query, ignoreCase = true) }
    }
}
