package com.example.safemotion.data.model

enum class InvitationStatus { PENDING, ACCEPTED, REJECTED }

enum class ActivityType { RUNNING, CYCLING }

data class Contact(
    val id: String,
    val name: String,
    val phone: String
)

data class Guardian(
    val id: String,
    val name: String,
    val phone: String,
    val status: InvitationStatus
)

data class ReceivedInvitation(
    val id: String,
    val runnerName: String,
    val activity: ActivityType,
    val sentAt: String
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
