package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MockUser

interface AuthRepository {
    val currentUser: MockUser?
    fun signIn(email: String, password: String): Boolean
    fun register(user: MockUser): Boolean
    fun signOut()
}
