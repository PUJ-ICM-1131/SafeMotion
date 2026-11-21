package com.example.safemotion.ui.screens.guardians

import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.Guardian
import com.example.safemotion.data.model.InvitationStatus
import com.example.safemotion.data.model.InviteResult
import com.example.safemotion.data.model.ReceivedInvitation
import com.example.safemotion.data.model.phoneKey
import com.example.safemotion.data.repository.GuardianRepository
import com.example.safemotion.ui.screens.invitations.InvitationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuardiansViewModelTest {
    private class FakeGuardianRepository : GuardianRepository {
        val guardians = MutableStateFlow<List<Guardian>>(emptyList())
        val received = MutableStateFlow<List<ReceivedInvitation>>(emptyList())
        val invited = mutableListOf<Contact>()
        val responses = mutableListOf<Pair<String, Boolean>>()
        val undone = mutableListOf<String>()

        override fun guardians(): Flow<List<Guardian>> = guardians
        override fun receivedInvitations(): Flow<List<ReceivedInvitation>> = received

        override suspend fun invite(contacts: List<Contact>): InviteResult {
            invited += contacts
            val withoutAccount = contacts.filter { it.name == "Sin cuenta" }.map { it.name }
            return InviteResult(contacts.size - withoutAccount.size, 0, withoutAccount)
        }

        override suspend fun respond(invitationId: String, accepted: Boolean) {
            responses += invitationId to accepted
        }

        override suspend fun undoResponse(invitationId: String) {
            undone += invitationId
        }
    }

    private val contacts = listOf(
        Contact("1", "Laura Gómez", "+57 320 776 1204"),
        Contact("2", "Sin cuenta", "300 111 2233")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun phoneKeyIgnoresFormatAndCountryCode() {
        assertEquals("3207761204", phoneKey("+57 320 776-1204"))
        assertEquals(phoneKey("320 776 1204"), phoneKey("(+57) 3207761204"))
    }

    @Test
    fun guardiansFollowTheRepositoryInRealTime() {
        val repository = FakeGuardianRepository()
        val viewModel = GuardiansViewModel(repository)
        viewModel.refresh()

        repository.guardians.value = listOf(Guardian("g1", "Laura", "320", InvitationStatus.PENDING))
        assertEquals(InvitationStatus.PENDING, viewModel.uiState.value.guardians.single().status)

        repository.guardians.value = listOf(Guardian("g1", "Laura", "320", InvitationStatus.ACCEPTED))
        assertEquals(InvitationStatus.ACCEPTED, viewModel.uiState.value.guardians.single().status)
    }

    @Test
    fun invitingReportsContactsWithoutAccountAndClearsSelection() {
        val repository = FakeGuardianRepository()
        val viewModel = GuardiansViewModel(repository)
        viewModel.loadContacts { contacts }
        viewModel.toggleContact("1")
        viewModel.toggleContact("2")

        viewModel.sendInvitations()

        val state = viewModel.uiState.value
        assertEquals(contacts, repository.invited)
        assertEquals(1, state.inviteResult?.sent)
        assertEquals(listOf("Sin cuenta"), state.inviteResult?.withoutAccount)
        assertTrue(state.selectedContactIds.isEmpty())
        assertFalse(state.isSending)
    }

    @Test
    fun contactsThatAreNotLoadedCannotBeSelected() {
        val viewModel = GuardiansViewModel(FakeGuardianRepository())
        viewModel.loadContacts { contacts }

        viewModel.toggleContact("no-existe")

        assertTrue(viewModel.uiState.value.selectedContactIds.isEmpty())
    }

    @Test
    fun respondingSavesTheAnswerAndCanBeUndone() {
        val repository = FakeGuardianRepository()
        repository.received.value = listOf(ReceivedInvitation("i1", "Camilo", "311", "21 nov"))
        val viewModel = InvitationsViewModel(repository)
        viewModel.refresh()

        assertTrue(viewModel.respond("i1", accepted = false))
        assertTrue(viewModel.uiState.value.invitations.isEmpty())
        assertEquals(listOf("i1" to false), repository.responses)

        viewModel.undoResponse()
        assertEquals(listOf("i1"), repository.undone)
    }
}
