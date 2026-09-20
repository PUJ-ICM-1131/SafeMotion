package com.example.safemotion.ui.screens.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType

@Composable
fun ActiveRunScreen(
    uiState: RunUiState,
    onSimulateIncident: (IncidentType) -> Unit,
    onFinishRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = uiState.session ?: return
    var showFinishConfirmation by rememberSaveable { mutableStateOf(false) }
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.active_run_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = if (session.activity == RunActivityType.RUNNING) {
                    stringResource(R.string.activity_running)
                } else {
                    stringResource(R.string.activity_cycling)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(
                                R.string.run_time,
                                session.elapsedSeconds / 60,
                                session.elapsedSeconds % 60
                            )
                        )
                        Text(stringResource(R.string.run_distance, session.distanceKm))
                    }
                    Text(stringResource(R.string.run_guardians_connected, session.guardianIds.size))
                    Text(stringResource(R.string.run_gps_mock))
                    Text(stringResource(R.string.run_sensors_mock))
                }
            }
            Text(
                text = stringResource(R.string.incident_demo_title),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedButton(
                onClick = { onSimulateIncident(IncidentType.FALL) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.simulate_fall)) }
            OutlinedButton(
                onClick = { onSimulateIncident(IncidentType.INACTIVITY) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.simulate_inactivity)) }
            Button(
                onClick = { showFinishConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.finish_run)) }
        }
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text(stringResource(R.string.finish_run)) },
            text = { Text(stringResource(R.string.finish_run_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    showFinishConfirmation = false
                    onFinishRun()
                }) { Text(stringResource(R.string.confirm_finish)) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text(stringResource(R.string.keep_running))
                }
            }
        )
    }
}
