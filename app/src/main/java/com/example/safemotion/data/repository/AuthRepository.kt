package com.example.safemotion.data.repository

import com.example.safemotion.data.model.AppUser

enum class AuthFailure { InvalidCredentials, ExistingEmail, WeakPassword, Network, Unknown }

interface AuthRepository {
    val currentUser: AppUser?
    fun restoreSession(): Boolean
    suspend fun signIn(email: String, password: String): AuthFailure?
    suspend fun register(name: String, email: String, password: String): AuthFailure?
    suspend fun sendPasswordReset(email: String): AuthFailure?
    fun signOut()
}
