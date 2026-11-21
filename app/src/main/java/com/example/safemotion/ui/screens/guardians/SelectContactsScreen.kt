package com.example.safemotion.ui.screens.guardians

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.InviteResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SelectContactsScreen(
    modifier: Modifier = Modifier,
    uiState: GuardiansUiState,
    onQueryChange: (String) -> Unit,
    onToggleContact: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
    onLoadContacts: () -> Unit,
    onPermissionDenied: () -> Unit,
    onResultDismissed: () -> Unit
) {
    // Permiso de contactos con Accompanist: sin él no se puede leer la agenda.
    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS) { granted ->
        if (granted) onLoadContacts() else onPermissionDenied()
    }
    val requestContacts = {
        if (contactsPermission.status.isGranted) {
            onLoadContacts()
        } else {
            contactsPermission.launchPermissionRequest()
        }
    }
    LaunchedEffect(Unit) { requestContacts() }

    uiState.inviteResult?.let { result ->
        InviteResultDialog(result = result, onDismiss = onResultDismissed)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invite_guardians_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface {
                Button(
                    onClick = onSend,
                    enabled = uiState.selectedContactIds.isNotEmpty() && !uiState.isSending,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    }
                    Text(
                        text = pluralStringResource(
                            R.plurals.send_invitations,
                            uiState.selectedContactIds.size,
                            uiState.selectedContactIds.size
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_contacts)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Text(
                text = pluralStringResource(
                    R.plurals.selected_contacts,
                    uiState.selectedContactIds.size,
                    uiState.selectedContactIds.size
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            if (uiState.inviteFailed) {
                ContactsMessage(text = stringResource(R.string.invite_failed), isError = true)
            }
            when {
                uiState.contactsPermissionDenied -> {
                    ContactsMessage(text = stringResource(R.string.contacts_permission_denied), isError = true)
                    TextButton(
                        onClick = requestContacts,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(stringResource(R.string.contacts_permission_retry))
                    }
                }
                uiState.isLoadingContacts -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.contacts.isEmpty() -> {
                    ContactsMessage(text = stringResource(R.string.contacts_empty), isError = false)
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(uiState.contacts, key = { it.id }) { contact ->
                            ContactRow(
                                contact = contact,
                                selected = contact.id in uiState.selectedContactIds,
                                onToggle = { onToggleContact(contact.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    modifier: Modifier = Modifier,
    contact: Contact,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .background(background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            InitialsAvatar(name = contact.name)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun ContactsMessage(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

// Resume qué invitaciones se enviaron y qué contactos todavía no tienen cuenta.
@Composable
private fun InviteResultDialog(result: InviteResult, onDismiss: () -> Unit) {
    val lines = buildList {
        if (result.sent > 0) {
            add(pluralStringResource(R.plurals.invitations_sent, result.sent, result.sent))
        }
        if (result.alreadyInvited > 0) {
            add(pluralStringResource(R.plurals.invitations_already, result.alreadyInvited, result.alreadyInvited))
        }
        if (result.withoutAccount.isNotEmpty()) {
            add(stringResource(R.string.invite_without_account, result.withoutAccount.joinToString(", ")))
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.understood)) }
        },
        title = { Text(stringResource(R.string.invite_result_title)) },
        text = { Text(lines.joinToString(separator = "\n\n")) }
    )
}
