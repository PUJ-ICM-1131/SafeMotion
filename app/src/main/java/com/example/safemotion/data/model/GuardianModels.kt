package com.example.safemotion.data.model

enum class InvitationStatus { PENDING, ACCEPTED, REJECTED }

data class Contact(
    val id: String,
    val name: String,
    val phone: String
)

// Una invitación enviada; el id es el del documento en Firestore.
data class Guardian(
    val id: String,
    val name: String,
    val phone: String,
    val status: InvitationStatus,
    val uid: String = ""
)

data class ReceivedInvitation(
    val id: String,
    val runnerName: String,
    val runnerPhone: String,
    val sentAt: String
)

// Resultado de invitar: cuántas se enviaron y qué contactos no tienen cuenta en SafeMotion.
data class InviteResult(
    val sent: Int,
    val alreadyInvited: Int,
    val withoutAccount: List<String>
)

fun firstNameOf(name: String): String = name.trim().substringBefore(" ")

fun initialsOf(name: String): String {
    val partes = name.trim().split(" ")
    return if (partes.size >= 2) {
        "${partes[0].first()}${partes[1].first()}".uppercase()
    } else {
        name.trim().take(2).uppercase()
    }
}
