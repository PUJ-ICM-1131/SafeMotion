package com.example.safemotion.ui.screens.receivedalert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonalInjury
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.IncidentType
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivedAlertScreen(
    uiState: ReceivedAlertUiState,
    onBack: () -> Unit,
    onOpenMap: (String) -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.received_alert_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        val alert = uiState.alert
        if (alert == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.received_alert_missing_title), style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.received_alert_missing_description),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.received_alert_recent), color = MaterialTheme.colorScheme.error)
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(alert.runnerName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(
                                R.string.received_alert_run_in_progress,
                                stringResource(
                                    if (uiState.monitoredSessionId == "session-laura") {
                                        R.string.monitoring_activity_cycling
                                    } else {
                                        R.string.monitoring_activity_running
                                    }
                                )
                            )
                        )
                    }
                }
                AlertDetail(
                    icon = Icons.Default.PersonalInjury,
                    label = stringResource(R.string.received_alert_type_label),
                    value = stringResource(
                        if (alert.incidentType == IncidentType.FALL) {
                            R.string.received_alert_fall
                        } else {
                            R.string.received_alert_inactivity
                        }
                    )
                )
                AlertDetail(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.received_alert_time_label),
                    value = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(alert.occurredAtMillis))
                )
                AlertDetail(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.received_alert_location_label),
                    value = alert.lastLocation
                )
                Text(
                    stringResource(R.string.received_alert_mock_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { uiState.monitoredSessionId?.let(onOpenMap) },
                    enabled = uiState.monitoredSessionId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Text(stringResource(R.string.received_alert_open_map), modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = onCall, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Text(
                        stringResource(R.string.received_alert_call, alert.runnerName.substringBefore(" ")),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
