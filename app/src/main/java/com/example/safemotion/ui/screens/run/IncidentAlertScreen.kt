package com.example.safemotion.ui.screens.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.IncidentType

@Composable
fun IncidentAlertScreen(
    uiState: RunUiState,
    onCancelAlert: () -> Unit,
    onReturnToRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.incident_alert_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (uiState.incidentType) {
                    IncidentType.FALL -> stringResource(R.string.incident_fall)
                    IncidentType.INACTIVITY -> stringResource(R.string.incident_inactivity)
                    null -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 20.dp)
            )
            if (uiState.alertSent) {
                Text(
                    text = stringResource(R.string.alert_sent_mock),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 28.dp)
                )
                Button(onClick = onReturnToRun, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.return_to_run))
                }
            } else {
                Text(
                    text = stringResource(R.string.alert_countdown, uiState.secondsToCancel),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 28.dp)
                )
                Text(
                    text = stringResource(R.string.alert_countdown_explanation),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = onCancelAlert,
                    enabled = uiState.secondsToCancel > 0,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text(stringResource(R.string.cancel_alert)) }
            }
        }
    }
}
