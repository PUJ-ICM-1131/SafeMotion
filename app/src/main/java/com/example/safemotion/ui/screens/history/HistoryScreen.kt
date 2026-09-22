package com.example.safemotion.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonalInjury
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.RunActivityType
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onStartRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        if (uiState.isEmpty) {
            EmptyHistory(
                onStartRun = onStartRun,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineMedium)
                }
                item {
                    Text(
                        stringResource(R.string.history_mock_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(uiState.runs, key = { it.id }) { run ->
                    HistoryCard(run = run)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(run: HistoryRunItem, modifier: Modifier = Modifier) {
    val durationHours = run.durationSeconds / 3_600
    val remainingMinutes = run.durationSeconds % 3_600 / 60
    val duration = if (durationHours > 0) {
        stringResource(R.string.history_duration_hours, durationHours, remainingMinutes)
    } else {
        stringResource(R.string.history_duration_minutes, remainingMinutes)
    }
    val activityLabel = stringResource(
        if (run.activity == RunActivityType.RUNNING) {
            R.string.monitoring_activity_running
        } else {
            R.string.monitoring_activity_cycling
        }
    )
    val (statusIcon, statusText) = when (run.status) {
        HistoryRunStatus.COMPLETED -> Icons.Default.CheckCircle to stringResource(R.string.history_status_completed)
        HistoryRunStatus.ALERT_SENT -> Icons.Default.PersonalInjury to stringResource(R.string.history_status_alert)
        HistoryRunStatus.INTERRUPTED -> Icons.Default.Warning to stringResource(R.string.history_status_interrupted)
    }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (run.activity == RunActivityType.RUNNING) {
                    Icons.AutoMirrored.Filled.DirectionsRun
                } else {
                    Icons.AutoMirrored.Filled.DirectionsBike
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(run.completedAtMillis)),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(R.string.history_run_detail, activityLabel, duration, run.distanceKm),
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = if (run.status == HistoryRunStatus.COMPLETED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Text(statusText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(onStartRun: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            stringResource(R.string.history_empty_description),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        Button(onClick = onStartRun) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.history_start_run), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
