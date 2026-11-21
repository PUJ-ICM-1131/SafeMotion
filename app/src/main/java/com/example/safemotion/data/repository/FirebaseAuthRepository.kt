package com.example.safemotion.data.repository

import com.example.safemotion.data.model.AppUser
import com.example.safemotion.data.model.phoneKey
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository : AuthRepository {
    const val USERS = "users"

    // Se obtiene al usarlo para que las pruebas unitarias no inicialicen Firebase.
    private val auth get() = Firebase.auth
    private val users get() = Firebase.firestore.collection(USERS)

    override var currentUser: AppUser? = null
        private set

    override fun restoreSession(): Boolean {
        currentUser = auth.currentUser?.toAppUser()
        return currentUser != null
    }

    override suspend fun signIn(email: String, password: String): AuthFailure? = runAuth {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user?.toAppUser()
        // Las cuentas creadas antes de Firestore no tenían perfil; se completa sin borrar el teléfono.
        // Si falla, el usuario igual puede entrar.
        user?.let {
            runCatching {
                users.document(it.uid)
                    .set(mapOf("name" to it.name, "email" to it.email), SetOptions.merge())
                    .await()
            }
        }
        currentUser = user
    }

    override suspend fun register(name: String, phone: String, email: String, password: String): AuthFailure? = runAuth {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user ?: return@runAuth
        user.updateProfile(userProfileChangeRequest { displayName = name }).await()
        // Perfil público para que otras cuentas encuentren a este usuario por su teléfono.
        users.document(user.uid).set(
            mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "phoneKey" to phoneKey(phone)
            )
        ).await()
        currentUser = AppUser(uid = user.uid, name = name, email = email)
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
        uid = uid,
        name = displayName.orEmpty(),
        email = email.orEmpty()
    )
}
