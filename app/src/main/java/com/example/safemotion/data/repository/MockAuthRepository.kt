package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MockUser

object MockAuthRepository : AuthRepository {
    private val users = mutableListOf(
        MockUser(
            name = "Mariana Rojas",
            phone = "3104827719",
            email = "mariana.rojas@correo.com",
            password = "clave1234"
        )
    )
    override var currentUser: MockUser? = null
        private set

    override fun signIn(email: String, password: String): Boolean {
        currentUser = users.find {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
        return currentUser != null
    }

    override fun register(user: MockUser): Boolean {
        if (users.any { it.email.equals(user.email.trim(), ignoreCase = true) }) return false
        users.add(user.copy(email = user.email.trim()))
        currentUser = user
        return true
    }

    override fun signOut() {
        currentUser = null
    }
}
