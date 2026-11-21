package com.example.safemotion.data.repository

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.InviteResult
import com.example.safemotion.data.model.ReceivedInvitation
import kotlinx.coroutines.flow.Flow

interface GuardianRepository {
    /** Invitaciones que envió el usuario actual, con su estado en tiempo real. */
    fun guardians(): Flow<List<Guardian>>

    /** Invitaciones pendientes que recibió el usuario actual, en tiempo real. */
    fun receivedInvitations(): Flow<List<ReceivedInvitation>>

    suspend fun invite(contacts: List<Contact>): InviteResult

    suspend fun respond(invitationId: String, accepted: Boolean)

    /** Deshace una respuesta: la invitación vuelve a quedar pendiente. */
    suspend fun undoResponse(invitationId: String)
}
