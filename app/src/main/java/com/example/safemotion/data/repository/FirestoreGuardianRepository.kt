package com.example.safemotion.data.repository

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.InviteResult
import com.example.safemotion.data.model.ReceivedInvitation
import com.example.safemotion.data.model.phoneKey
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Guardianes e invitaciones compartidos entre cuentas con Cloud Firestore.
 *
 * Colección invitations/{id}: fromUid, fromName, fromPhone, toUid, toName, toPhone, status, createdAt.
 * Un guardián es una invitación enviada; si está ACCEPTED, esa persona acompaña al usuario.
 */
object FirestoreGuardianRepository : GuardianRepository {
    const val INVITATIONS = "invitations"

    private val db get() = Firebase.firestore
    private val invitations get() = db.collection(INVITATIONS)
    private val currentUid get() = Firebase.auth.currentUser?.uid

    private val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.forLanguageTag("es-CO"))

    override fun guardians(): Flow<List<Guardian>> {
        val uid = currentUid ?: return flowOf(emptyList())
        return invitations.whereEqualTo("fromUid", uid).snapshots().map { snapshot ->
            snapshot.documents.map { it.toGuardian() }.sortedBy { it.name.lowercase() }
        }
    }

    override fun receivedInvitations(): Flow<List<ReceivedInvitation>> {
        val uid = currentUid ?: return flowOf(emptyList())
        return invitations
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", InvitationStatus.PENDING.name)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .sortedByDescending { it.getTimestamp("createdAt")?.seconds ?: Long.MAX_VALUE }
                    .map { it.toReceivedInvitation() }
            }
    }

    override suspend fun invite(contacts: List<Contact>): InviteResult {
        val user = Firebase.auth.currentUser ?: return InviteResult(0, 0, contacts.map { it.name })
        val myProfile = db.collection(FirebaseAuthRepository.USERS).document(user.uid).get().await()
        var sent = 0
        var alreadyInvited = 0
        val withoutAccount = mutableListOf<String>()

        for (contact in contacts) {
            // El contacto se relaciona con una cuenta por su número de teléfono.
            val target = db.collection(FirebaseAuthRepository.USERS)
                .whereEqualTo("phoneKey", phoneKey(contact.phone))
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
            if (target == null) {
                withoutAccount += contact.name
                continue
            }
            if (target.id == user.uid) continue

            val existing = invitations
                .whereEqualTo("fromUid", user.uid)
                .whereEqualTo("toUid", target.id)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
            when {
                existing == null -> {
                    invitations.add(
                        mapOf(
                            "fromUid" to user.uid,
                            "fromName" to (user.displayName ?: myProfile.getString("name").orEmpty()),
                            "fromPhone" to myProfile.getString("phone").orEmpty(),
                            "toUid" to target.id,
                            "toName" to contact.name,
                            "toPhone" to contact.phone,
                            "status" to InvitationStatus.PENDING.name,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    sent++
                }
                // Si la habían rechazado, se vuelve a enviar.
                existing.getString("status") == InvitationStatus.REJECTED.name -> {
                    existing.reference.update(
                        mapOf(
                            "status" to InvitationStatus.PENDING.name,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    sent++
                }
                else -> alreadyInvited++
            }
        }
        return InviteResult(sent, alreadyInvited, withoutAccount)
    }

    override suspend fun respond(invitationId: String, accepted: Boolean) {
        val status = if (accepted) InvitationStatus.ACCEPTED else InvitationStatus.REJECTED
        invitations.document(invitationId).update(
            mapOf("status" to status.name, "respondedAt" to FieldValue.serverTimestamp())
        ).await()
    }

    override suspend fun undoResponse(invitationId: String) {
        invitations.document(invitationId).update(
            mapOf("status" to InvitationStatus.PENDING.name, "respondedAt" to null)
        ).await()
    }

    private fun DocumentSnapshot.toGuardian() = Guardian(
        id = id,
        name = getString("toName").orEmpty(),
        phone = getString("toPhone").orEmpty(),
        status = getString("status")
            ?.let { runCatching { InvitationStatus.valueOf(it) }.getOrNull() }
            ?: InvitationStatus.PENDING
    )

    private fun DocumentSnapshot.toReceivedInvitation() = ReceivedInvitation(
        id = id,
        runnerName = getString("fromName").orEmpty(),
        runnerPhone = getString("fromPhone").orEmpty(),
        sentAt = getTimestamp("createdAt")?.format().orEmpty()
    )

    private fun Timestamp.format(): String = dateFormat.format(toDate())
}
