package com.example.safemotion.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.safemotion.data.repository.MockAuthRepository
import com.example.safemotion.data.repository.MockGuardianTrackingRepository
import com.example.safemotion.data.repository.MockRunRepository
import com.example.safemotion.ui.components.MainDestination
import com.example.safemotion.ui.components.MainNavigationBar
import com.example.safemotion.ui.screens.auth.AuthScreen
import com.example.safemotion.ui.screens.auth.AuthViewModel
import com.example.safemotion.ui.screens.home.HomeScreen
import com.example.safemotion.ui.screens.home.HomeViewModel
import com.example.safemotion.ui.screens.history.HistoryScreen
import com.example.safemotion.ui.screens.history.HistoryViewModel
import com.example.safemotion.ui.screens.monitoring.MonitoringScreen
import com.example.safemotion.ui.screens.monitoring.MonitoringViewModel
import com.example.safemotion.ui.screens.receivedalert.ReceivedAlertScreen
import com.example.safemotion.ui.screens.receivedalert.ReceivedAlertViewModel
import com.example.safemotion.ui.screens.guardians.GuardiansScreen
import com.example.safemotion.ui.screens.guardians.GuardiansViewModel
import com.example.safemotion.ui.screens.guardians.SelectContactsScreen
import com.example.safemotion.ui.screens.invitations.InvitationsScreen
import com.example.safemotion.ui.screens.invitations.InvitationsViewModel
import com.example.safemotion.ui.screens.run.ActiveRunScreen
import com.example.safemotion.ui.screens.run.IncidentAlertScreen
import com.example.safemotion.ui.screens.run.PrepareRunScreen
import com.example.safemotion.ui.screens.run.RunViewModel
import com.example.safemotion.ui.screens.reportrisk.ReportRiskScreen
import com.example.safemotion.ui.screens.reportrisk.ReportRiskViewModel
import com.example.safemotion.ui.screens.riskmap.RiskMapScreen
import com.example.safemotion.ui.screens.riskmap.RiskMapViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AuthRoute)
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val runViewModel: RunViewModel = viewModel()
    val guardiansViewModel: GuardiansViewModel = viewModel()
    val invitationsViewModel: InvitationsViewModel = viewModel()
    val riskMapViewModel: RiskMapViewModel = viewModel()
    val reportRiskViewModel: ReportRiskViewModel = viewModel()
    val monitoringViewModel: MonitoringViewModel = viewModel()
    val receivedAlertViewModel: ReceivedAlertViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
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
    val selectedDestination = when (backStack.lastOrNull()) {
        HomeRoute -> MainDestination.HOME
        GuardiansRoute -> MainDestination.GUARDIANS
        RiskMapRoute -> MainDestination.RISKS
        HistoryRoute -> MainDestination.HISTORY
        else -> null
    }
    fun openMainDestination(destination: MainDestination) {
        val route = when (destination) {
            MainDestination.HOME -> HomeRoute
            MainDestination.GUARDIANS -> GuardiansRoute
            MainDestination.RISKS -> RiskMapRoute
            MainDestination.HISTORY -> HistoryRoute
        }
        if (backStack.lastOrNull() == route) return
        backStack.clear()
        backStack.add(HomeRoute)
        if (route != HomeRoute) backStack.add(route)
    }
    Scaffold(
        modifier = modifier,
        bottomBar = {
            selectedDestination?.let { selected ->
                MainNavigationBar(
                    selected = selected,
                    onDestinationSelected = ::openMainDestination
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
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
                    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        homeViewModel.refresh()
                    }
                    HomeScreen(
                        uiState = uiState,
                        onPrepareRun = { backStack.add(PrepareRunRoute) },
                        onGuardians = { backStack.add(GuardiansRoute) },
                        onInvitations = { backStack.add(InvitationsRoute) },
                        onMonitoring = { backStack.add(MonitoringRoute()) },
                        onReceivedAlert = { backStack.add(ReceivedAlertRoute()) },
                        onRiskMap = { backStack.add(RiskMapRoute) },
                        onReportRisk = { backStack.add(ReportRiskRoute) },
                        onHistory = { backStack.add(HistoryRoute) },
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
                RiskMapRoute -> NavEntry(key) {
                    val uiState by riskMapViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        riskMapViewModel.refresh()
                    }
                    RiskMapScreen(
                        uiState = uiState,
                        onZoneSelected = riskMapViewModel::selectZone,
                        onDismissZone = riskMapViewModel::dismissZone,
                        onConfirmZone = riskMapViewModel::confirmZone,
                        onReport = { backStack.add(ReportRiskRoute) }
                    )
                }
                ReportRiskRoute -> NavEntry(key) {
                    val uiState by reportRiskViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        reportRiskViewModel.prepareNewReport()
                    }
                    ReportRiskScreen(
                        uiState = uiState,
                        onCategorySelected = reportRiskViewModel::selectCategory,
                        onDescriptionChange = reportRiskViewModel::changeDescription,
                        onUseCurrentLocation = reportRiskViewModel::useCurrentLocation,
                        onAttachPhoto = reportRiskViewModel::attachPhoto,
                        onRemovePhoto = reportRiskViewModel::removePhoto,
                        onSubmit = reportRiskViewModel::submit,
                        onReportSent = { backStack.removeLastOrNull() },
                        onClose = {
                            reportRiskViewModel.reset()
                            backStack.removeLastOrNull()
                        }
                    )
                }
                is MonitoringRoute -> NavEntry(key) {
                    LaunchedEffect(key.sessionId) {
                        monitoringViewModel.selectSession(key.sessionId)
                    }
                    val uiState by monitoringViewModel.uiState.collectAsStateWithLifecycle()
                    MonitoringScreen(
                        uiState = uiState,
                        onBack = { backStack.removeLastOrNull() },
                        onCall = {
                            uiState.session?.phone?.let { phone -> dialPhone(context, phone) }
                        }
                    )
                }
                is ReceivedAlertRoute -> NavEntry(key) {
                    LaunchedEffect(key.alertId) {
                        receivedAlertViewModel.selectAlert(key.alertId)
                    }
                    val uiState by receivedAlertViewModel.uiState.collectAsStateWithLifecycle()
                    ReceivedAlertScreen(
                        uiState = uiState,
                        onBack = { backStack.removeLastOrNull() },
                        onOpenMap = { sessionId -> backStack.add(MonitoringRoute(sessionId)) },
                        onCall = {
                            uiState.monitoredSessionId
                                ?.let(MockGuardianTrackingRepository::findSession)
                                ?.phone
                                ?.let { phone -> dialPhone(context, phone) }
                        }
                    )
                }
                HistoryRoute -> NavEntry(key) {
                    LaunchedEffect(Unit) {
                        historyViewModel.refresh()
                    }
                    val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()
                    HistoryScreen(
                        uiState = uiState,
                        onStartRun = { backStack.add(PrepareRunRoute) }
                    )
                }
                else -> error("Ruta no registrada: $key")
                }
            }
        )
    }
}

private fun dialPhone(context: Context, phone: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.filter(Char::isDigit)}"))
    context.startActivity(dialIntent)
}
