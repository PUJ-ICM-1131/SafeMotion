package com.example.safemotion.data.repository

import com.example.safemotion.data.model.ActivityType
import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.ReceivedInvitation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockGuardianRepository {
    val deviceContacts = listOf(
        Contact("c1", "Juliana Mejía", "312 664 9032"),
        Contact("c2", "Daniel Ospina", "301 228 4417"),
        Contact("c3", "Valentina Cruz", "315 780 2211"),
        Contact("c4", "Jorge Tobón", "319 441 7765"),
        Contact("c5", "Paula Bernal", "300 119 5508")
    )

    private val initialGuardians = listOf(
        Guardian("g1", "Camilo Restrepo", "311 245 8890", InvitationStatus.ACCEPTED),
        Guardian("g2", "Laura Guzmán", "320 776 1204", InvitationStatus.ACCEPTED),
        Guardian("g3", "Andrés Peñaloza", "318 902 3355", InvitationStatus.PENDING),
        Guardian("g4", "Sofía Cárdenas", "314 550 6721", InvitationStatus.REJECTED)
    )

    private val initialInvitations = listOf(
        ReceivedInvitation("i1", "Andrés Peñaloza", ActivityType.CYCLING, "16 sep, 8:12 p. m."),
        ReceivedInvitation("i2", "Sofía Cárdenas", ActivityType.RUNNING, "15 sep, 6:40 a. m.")
    )

    private val _guardians = MutableStateFlow(initialGuardians)
    val guardians = _guardians.asStateFlow()

    private val _receivedInvitations = MutableStateFlow(initialInvitations)
    val receivedInvitations = _receivedInvitations.asStateFlow()

    fun invite(contacts: List<Contact>) {
        val idsActuales = _guardians.value.map { it.id }
        val nuevos = contacts
            .filter { it.id !in idsActuales }
            .map { Guardian(it.id, it.name, it.phone, InvitationStatus.PENDING) }
        _guardians.value = _guardians.value + nuevos
    }

    fun respond(invitationId: String) {
        _receivedInvitations.value = _receivedInvitations.value.filter { it.id != invitationId }
    }

    fun restore(invitation: ReceivedInvitation, position: Int) {
        val lista = _receivedInvitations.value.toMutableList()
        if (position >= 0 && position <= lista.size) {
            lista.add(position, invitation)
        } else {
            lista.add(invitation)
        }
        _receivedInvitations.value = lista
    }

    fun clearForSignOut() {
        _guardians.value = initialGuardians
        _receivedInvitations.value = initialInvitations
    }
}
