package com.example.safemotion.ui.screens.auth

enum class AuthMode { SignIn, Register, Recover }

enum class AuthMessage {
    MissingFields,
    InvalidEmail,
    ShortPassword,
    InvalidCredentials,
    ExistingEmail,
    NetworkError,
    UnknownError,
    RecoverySent
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val message: AuthMessage? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false
)
