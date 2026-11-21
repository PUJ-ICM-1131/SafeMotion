package com.example.safemotion.ui.screens.guardians

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.repository.FirestoreGuardianRepository
import com.example.safemotion.data.repository.GuardianRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardiansViewModel(
    private val repository: GuardianRepository = FirestoreGuardianRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuardiansUiState())
    val uiState = _uiState.asStateFlow()

    private var todosLosContactos: List<Contact> = emptyList()
    private var guardiansJob: Job? = null

    // Se vuelve a suscribir cada vez que se abre la pantalla, por si cambió la sesión.
    fun refresh() {
        guardiansJob?.cancel()
        guardiansJob = viewModelScope.launch {
            repository.guardians()
                .catch { }
                .collect { guardians -> _uiState.update { it.copy(guardians = guardians) } }
        }
    }

    // Se llama cuando el usuario ya dio el permiso de contactos.
    fun loadContacts(readContacts: suspend () -> List<Contact>) {
        _uiState.update { it.copy(isLoadingContacts = true, contactsPermissionDenied = false) }
        viewModelScope.launch {
            todosLosContactos = readContacts()
            _uiState.update {
                it.copy(
                    isLoadingContacts = false,
                    contacts = buscar(it.searchQuery),
                    selectedContactIds = it.selectedContactIds.filterTo(mutableSetOf()) { id ->
                        todosLosContactos.any { contact -> contact.id == id }
                    }
                )
            }
        }
    }

    fun onContactsPermissionDenied() {
        _uiState.update { it.copy(isLoadingContacts = false, contactsPermissionDenied = true) }
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

    fun sendInvitations() {
        val state = _uiState.value
        if (state.selectedContactIds.isEmpty() || state.isSending) return
        val elegidos = todosLosContactos.filter { it.id in state.selectedContactIds }
        _uiState.update { it.copy(isSending = true, inviteFailed = false) }
        viewModelScope.launch {
            try {
                val result = repository.invite(elegidos)
                _uiState.update {
                    it.copy(
                        isSending = false,
                        inviteResult = result,
                        contacts = todosLosContactos,
                        searchQuery = "",
                        selectedContactIds = emptySet()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, inviteFailed = true) }
            }
        }
    }

    fun consumeInviteResult() {
        _uiState.update { it.copy(inviteResult = null, inviteFailed = false) }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                contacts = todosLosContactos,
                searchQuery = "",
                selectedContactIds = emptySet(),
                inviteResult = null,
                inviteFailed = false
            )
        }
    }

    private fun buscar(query: String): List<Contact> {
        if (query.isBlank()) return todosLosContactos
        return todosLosContactos.filter { it.name.contains(query, ignoreCase = true) }
    }
}
