package com.example.safemotion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.safemotion.ui.screens.home.HomeScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeRoute)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                HomeRoute -> NavEntry(key) { HomeScreen() }
                else -> error("Ruta no registrada: $key")
            }
        }
    )
}
