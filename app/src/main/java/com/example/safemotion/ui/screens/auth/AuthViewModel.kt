package com.example.safemotion.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.model.MockUser
import com.example.safemotion.data.repository.AuthRepository
import com.example.safemotion.data.repository.MockAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    private val repository: AuthRepository = MockAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

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
        val email = state.email.trim()
        val message = when (state.mode) {
            AuthMode.SignIn -> when {
                email.isEmpty() || state.password.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                repository.signIn(email, state.password) -> null
                else -> AuthMessage.InvalidCredentials
            }
            AuthMode.Register -> when {
                state.name.isBlank() || state.phone.isBlank() ||
                    email.isEmpty() || state.password.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                state.password.length < 8 -> AuthMessage.ShortPassword
                repository.register(
                    MockUser(state.name.trim(), state.phone.trim(), email, state.password)
                ) -> null
                else -> AuthMessage.ExistingEmail
            }
            AuthMode.Recover -> when {
                email.isEmpty() -> AuthMessage.MissingFields
                !email.contains("@") -> AuthMessage.InvalidEmail
                else -> AuthMessage.RecoverySent
            }
        }
        _uiState.update {
            it.copy(
                message = message,
                isAuthenticated = message == null && state.mode != AuthMode.Recover
            )
        }
    }
}
