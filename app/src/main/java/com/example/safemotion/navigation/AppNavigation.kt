package com.example.safemotion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.safemotion.data.repository.MockAuthRepository
import com.example.safemotion.data.repository.MockRunRepository
import com.example.safemotion.ui.screens.auth.AuthScreen
import com.example.safemotion.ui.screens.auth.AuthViewModel
import com.example.safemotion.ui.screens.home.HomeScreen
import com.example.safemotion.ui.screens.home.HomeViewModel
import com.example.safemotion.ui.screens.guardians.GuardiansScreen
import com.example.safemotion.ui.screens.guardians.GuardiansViewModel
import com.example.safemotion.ui.screens.guardians.SelectContactsScreen
import com.example.safemotion.ui.screens.invitations.InvitationsScreen
import com.example.safemotion.ui.screens.invitations.InvitationsViewModel
import com.example.safemotion.ui.screens.run.ActiveRunScreen
import com.example.safemotion.ui.screens.run.IncidentAlertScreen
import com.example.safemotion.ui.screens.run.PrepareRunScreen
import com.example.safemotion.ui.screens.run.RunViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AuthRoute)
    val authViewModel: AuthViewModel = viewModel()
    val runViewModel: RunViewModel = viewModel()
    val guardiansViewModel: GuardiansViewModel = viewModel()
    val invitationsViewModel: InvitationsViewModel = viewModel()
    LaunchedEffect(Unit) {
        if (MockAuthRepository.currentUser == null && backStack.lastOrNull() != AuthRoute) {
            backStack.clear()
            backStack.add(AuthRoute)
        } else if (MockRunRepository.activeRun.value == null &&
            (backStack.lastOrNull() == ActiveRunRoute ||
                backStack.lastOrNull() == IncidentAlertRoute)
        ) {
            backStack.clear()
            backStack.add(HomeRoute)
        }
    }
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1 &&
                backStack.lastOrNull() != ActiveRunRoute &&
                backStack.lastOrNull() != IncidentAlertRoute
            ) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = { key ->
            when (key) {
                AuthRoute -> NavEntry(key) {
                    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(uiState.isAuthenticated) {
                        if (uiState.isAuthenticated) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        }
                    }
                    AuthScreen(
                        uiState = uiState,
                        onModeChange = authViewModel::changeMode,
                        onNameChange = authViewModel::changeName,
                        onPhoneChange = authViewModel::changePhone,
                        onEmailChange = authViewModel::changeEmail,
                        onPasswordChange = authViewModel::changePassword,
                        onSubmit = authViewModel::submit
                    )
                }
                HomeRoute -> NavEntry(key) {
                    val homeViewModel: HomeViewModel = viewModel()
                    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        homeViewModel.refresh()
                    }
                    HomeScreen(
                        uiState = uiState,
                        onPrepareRun = { backStack.add(PrepareRunRoute) },
                        onSignOut = {
                            authViewModel.signOut()
                            MockRunRepository.clearForSignOut()
                            backStack.clear()
                            backStack.add(AuthRoute)
                        }
                    )
                }
                PrepareRunRoute -> NavEntry(key) {
                    val uiState by runViewModel.uiState.collectAsStateWithLifecycle()
                    PrepareRunScreen(
                        uiState = uiState,
                        onActivitySelected = runViewModel::selectActivity,
                        onGuardianSelected = runViewModel::toggleGuardian,
                        onStartRun = {
                            if (runViewModel.startRun()) backStack.add(ActiveRunRoute)
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                ActiveRunRoute -> NavEntry(key) {
                    val uiState by runViewModel.uiState.collectAsStateWithLifecycle()
                    ActiveRunScreen(
                        uiState = uiState,
                        onSimulateIncident = { type ->
                            if (runViewModel.simulateIncident(type)) {
                                backStack.add(IncidentAlertRoute)
                            }
                        },
                        onFinishRun = {
                            if (runViewModel.finishRun()) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                            }
                        }
                    )
                }
                IncidentAlertRoute -> NavEntry(key) {
                    val uiState by runViewModel.uiState.collectAsStateWithLifecycle()
                    IncidentAlertScreen(
                        uiState = uiState,
                        onCancelAlert = {
                            if (runViewModel.cancelAlert()) backStack.removeLastOrNull()
                        },
                        onReturnToRun = {
                            if (runViewModel.returnToRun()) backStack.removeLastOrNull()
                        }
                    )
                }
                GuardiansRoute -> NavEntry(key) {
                    val uiState by guardiansViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        guardiansViewModel.refresh()
                    }
                    GuardiansScreen(
                        uiState = uiState,
                        onInvite = { backStack.add(SelectContactsRoute) }
                    )
                }
                SelectContactsRoute -> NavEntry(key) {
                    val uiState by guardiansViewModel.uiState.collectAsStateWithLifecycle()
                    SelectContactsScreen(
                        uiState = uiState,
                        onQueryChange = guardiansViewModel::changeSearchQuery,
                        onToggleContact = guardiansViewModel::toggleContact,
                        onSend = {
                            if (guardiansViewModel.sendInvitations()) {
                                backStack.removeLastOrNull()
                            }
                        },
                        onClose = {
                            guardiansViewModel.clearSelection()
                            backStack.removeLastOrNull()
                        }
                    )
                }
                InvitationsRoute -> NavEntry(key) {
                    val uiState by invitationsViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        invitationsViewModel.refresh()
                    }
                    InvitationsScreen(
                        uiState = uiState,
                        onRespond = { id -> invitationsViewModel.respond(id) },
                        onUndo = invitationsViewModel::undoResponse,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                else -> error("Ruta no registrada: $key")
            }
        }
    )
}
