package com.example.safemotion.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.repository.AuthFailure
import com.example.safemotion.data.repository.AuthRepository
import com.example.safemotion.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun restoreSession(): Boolean = repository.restoreSession()

    fun changeMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, message = null, password = "") }
    }

    fun changeName(value: String) {
        _uiState.update { it.copy(name = value, message = null) }
    }

    fun changePhone(value: String) {
        _uiState.update { it.copy(phone = value, message = null) }
    }

    fun changeEmail(value: String) {
        _uiState.update { it.copy(email = value, message = null) }
    }

    fun changePassword(value: String) {
        _uiState.update { it.copy(password = value, message = null) }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        val email = state.email.trim()
        val validationMessage = when (state.mode) {
            AuthMode.SignIn -> when {
                email.isEmpty() || state.password.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                else -> null
            }
            AuthMode.Register -> when {
                state.name.isBlank() || state.phone.isBlank() ||
                    email.isEmpty() || state.password.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                state.password.length < 8 -> AuthMessage.ShortPassword
                else -> null
            }
            AuthMode.Recover -> when {
                email.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                else -> null
            }
        }
        if (validationMessage != null) {
            _uiState.update { it.copy(message = validationMessage) }
            return
        }

        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val failure = when (state.mode) {
                AuthMode.SignIn -> repository.signIn(email, state.password)
                AuthMode.Register -> repository.register(state.name.trim(), email, state.password)
                AuthMode.Recover -> repository.sendPasswordReset(email)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = failure?.toMessage()
                        ?: if (state.mode == AuthMode.Recover) AuthMessage.RecoverySent else null,
                    isAuthenticated = failure == null && state.mode != AuthMode.Recover
                )
            }
        }
    }

    private fun AuthFailure.toMessage() = when (this) {
        AuthFailure.InvalidCredentials -> AuthMessage.InvalidCredentials
        AuthFailure.ExistingEmail -> AuthMessage.ExistingEmail
        AuthFailure.WeakPassword -> AuthMessage.ShortPassword
        AuthFailure.Network -> AuthMessage.NetworkError
        AuthFailure.Unknown -> AuthMessage.UnknownError
    }
}
