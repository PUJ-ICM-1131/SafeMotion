package com.example.safemotion.data.repository

import com.example.safemotion.data.model.AppUser
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository : AuthRepository {
    // Se obtiene al usarlo para que las pruebas unitarias no inicialicen Firebase.
    private val auth get() = Firebase.auth

    override var currentUser: AppUser? = null
        private set

    override fun restoreSession(): Boolean {
        currentUser = auth.currentUser?.toAppUser()
        return currentUser != null
    }

    override suspend fun signIn(email: String, password: String): AuthFailure? = runAuth {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        currentUser = result.user?.toAppUser()
    }

    override suspend fun register(name: String, email: String, password: String): AuthFailure? = runAuth {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
        user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
        currentUser = AppUser(name = name, email = email)
    }

    override suspend fun sendPasswordReset(email: String): AuthFailure? = runAuth {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() {
        auth.signOut()
        currentUser = null
    }

    private suspend fun runAuth(block: suspend () -> Unit): AuthFailure? = try {
        block()
        null
    } catch (e: FirebaseAuthWeakPasswordException) {
        AuthFailure.WeakPassword
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        AuthFailure.InvalidCredentials
    } catch (e: FirebaseAuthInvalidUserException) {
        AuthFailure.InvalidCredentials
    } catch (e: FirebaseAuthUserCollisionException) {
        AuthFailure.ExistingEmail
    } catch (e: FirebaseNetworkException) {
        AuthFailure.Network
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AuthFailure.Unknown
    }

    private fun FirebaseUser.toAppUser() = AppUser(
        name = displayName.orEmpty(),
        email = email.orEmpty()
    )
}
