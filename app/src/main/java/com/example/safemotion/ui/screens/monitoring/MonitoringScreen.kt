package com.example.safemotion.ui.screens.monitoring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.TrackingStatus
import com.example.safemotion.ui.components.TrackingMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(
    uiState: MonitoringUiState,
    onBack: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.session?.runnerName ?: stringResource(R.string.monitoring_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        val session = uiState.session
        if (session == null) {
            MissingMonitoringState(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            val interrupted = session.status == TrackingStatus.INTERRUPTED
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (interrupted) Icons.Default.Warning else Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (interrupted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            if (interrupted) R.string.monitoring_interrupted_alert
                            else R.string.monitoring_live_location
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(session.runnerName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(
                                R.string.monitoring_activity_place,
                                stringResource(
                                    if (session.activity == RunActivityType.RUNNING) {
                                        R.string.monitoring_activity_running
                                    } else {
                                        R.string.monitoring_activity_cycling
                                    }
                                ),
                                session.location
                            )
                        )
                        Text(
                            text = stringResource(
                                when (session.status) {
                                    TrackingStatus.ACTIVE -> R.string.monitoring_status_active
                                    TrackingStatus.INTERRUPTED -> R.string.monitoring_status_interrupted
                                    TrackingStatus.FINISHED -> R.string.monitoring_status_finished
                                }
                            ),
                            color = if (interrupted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = stringResource(R.string.monitoring_elapsed),
                        value = stringResource(
                            R.string.monitoring_duration_value,
                            session.elapsedSeconds / 60,
                            session.elapsedSeconds % 60
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = stringResource(R.string.monitoring_distance),
                        value = stringResource(R.string.monitoring_distance_value, session.distanceKm),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = if (session.lastUpdateSeconds < 60) {
                        pluralStringResource(
                            R.plurals.monitoring_last_update_seconds,
                            session.lastUpdateSeconds,
                            session.lastUpdateSeconds
                        )
                    } else {
                        val minutes = session.lastUpdateSeconds / 60
                        pluralStringResource(R.plurals.monitoring_last_update_minutes, minutes, minutes)
                    },
                    color = if (interrupted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                TrackingMap(
                    route = session.route,
                    interrupted = interrupted,
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Column {
                        Text(stringResource(R.string.monitoring_current_location), style = MaterialTheme.typography.labelLarge)
                        Text(session.location)
                    }
                }
                Text(
                    stringResource(R.string.monitoring_mock_notice),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onCall, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Text(
                        stringResource(R.string.monitoring_call, session.runnerName.substringBefore(" ")),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun MissingMonitoringState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.monitoring_missing_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.monitoring_missing_description),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
