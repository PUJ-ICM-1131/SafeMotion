package com.example.safemotion.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSignOut: () -> Unit,
    onPrepareRun: () -> Unit,
    onGuardians: () -> Unit,
    onInvitations: () -> Unit,
    onMonitoring: () -> Unit,
    onReceivedAlert: () -> Unit,
    onRiskMap: () -> Unit,
    onReportRisk: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.hello_user, uiState.firstName),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TextButton(onClick = onSignOut) { Text(stringResource(R.string.sign_out)) }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.home_safety_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.accepted_guardians,
                                uiState.acceptedGuardians,
                                uiState.acceptedGuardians
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.last_run_distance, uiState.lastDistanceKm),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.mock_sensor_status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Button(onClick = onPrepareRun, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text(stringResource(R.string.prepare_run), modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                Text(stringResource(R.string.home_quick_access), style = MaterialTheme.typography.titleLarge)
            }
            item { HomeAction(Icons.Default.Shield, R.string.home_guardians, onGuardians) }
            item { HomeAction(Icons.Default.Mail, R.string.home_invitations, onInvitations) }
            item { HomeAction(Icons.Default.Visibility, R.string.home_monitoring, onMonitoring) }
            item { HomeAction(Icons.Default.NotificationsActive, R.string.home_received_alert, onReceivedAlert) }
            item { HomeAction(Icons.Default.Map, R.string.home_risk_map, onRiskMap) }
            item { HomeAction(Icons.Default.Warning, R.string.home_report_risk, onReportRisk) }
            item { HomeAction(Icons.Default.History, R.string.home_history, onHistory) }
        }
    }
}

@Composable
private fun HomeAction(
    icon: ImageVector,
    labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null)
        Text(stringResource(labelRes), modifier = Modifier.padding(start = 10.dp).weight(1f))
    }
}
