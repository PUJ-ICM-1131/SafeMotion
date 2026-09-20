package com.example.safemotion.ui.screens.auth

enum class AuthMode { SignIn, Register, Recover }

enum class AuthMessage {
    MissingFields,
    InvalidEmail,
    ShortPassword,
    InvalidCredentials,
    ExistingEmail,
    RecoverySent
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val message: AuthMessage? = null,
    val isAuthenticated: Boolean = false
)
