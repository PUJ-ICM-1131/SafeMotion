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
import com.example.safemotion.ui.screens.auth.AuthScreen
import com.example.safemotion.ui.screens.auth.AuthViewModel
import com.example.safemotion.ui.screens.home.HomeScreen
import com.example.safemotion.ui.screens.home.HomeViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AuthRoute)
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(Unit) {
        if (MockAuthRepository.currentUser == null && backStack.lastOrNull() == HomeRoute) {
            backStack.clear()
            backStack.add(AuthRoute)
        }
    }
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
                        onSignOut = {
                            authViewModel.signOut()
                            backStack.clear()
                            backStack.add(AuthRoute)
                        }
                    )
                }
                else -> error("Ruta no registrada: $key")
            }
        }
    )
}
