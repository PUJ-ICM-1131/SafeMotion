package com.example.safemotion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.safemotion.R

enum class MainDestination { HOME, GUARDIANS, RISKS, HISTORY }

private data class MainNavigationItem(
    val destination: MainDestination,
    val labelRes: Int,
    val icon: ImageVector
)

@Composable
fun MainNavigationBar(
    selected: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        MainNavigationItem(MainDestination.HOME, R.string.main_nav_home, Icons.Default.Home),
        MainNavigationItem(MainDestination.GUARDIANS, R.string.main_nav_guardians, Icons.Default.Shield),
        MainNavigationItem(MainDestination.RISKS, R.string.main_nav_risks, Icons.Default.Warning),
        MainNavigationItem(MainDestination.HISTORY, R.string.main_nav_history, Icons.Default.History)
    )
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selected == item.destination,
                onClick = { onDestinationSelected(item.destination) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}
