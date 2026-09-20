package com.example.safemotion.ui.screens.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.RunActivityType

@Composable
fun PrepareRunScreen(
    uiState: RunUiState,
    onActivitySelected: (RunActivityType) -> Unit,
    onGuardianSelected: (Int) -> Unit,
    onStartRun: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.back_to_home)) }
            Text(
                text = stringResource(R.string.prepare_run_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.activity_type),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.activity == RunActivityType.RUNNING,
                    onClick = { onActivitySelected(RunActivityType.RUNNING) },
                    label = { Text(stringResource(R.string.activity_running)) }
                )
                FilterChip(
                    selected = uiState.activity == RunActivityType.CYCLING,
                    onClick = { onActivitySelected(RunActivityType.CYCLING) },
                    label = { Text(stringResource(R.string.activity_cycling)) }
                )
            }
            Text(
                text = stringResource(R.string.select_guardians),
                style = MaterialTheme.typography.titleMedium
            )
            uiState.guardians.forEach { guardian ->
                Card(
                    onClick = { onGuardianSelected(guardian.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(guardian.name, style = MaterialTheme.typography.bodyLarge)
                        Checkbox(
                            checked = guardian.id in uiState.selectedGuardianIds,
                            onCheckedChange = { onGuardianSelected(guardian.id) }
                        )
                    }
                }
            }
            if (uiState.needsGuardian) {
                Text(
                    text = stringResource(R.string.guardian_required),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = stringResource(R.string.run_mock_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onStartRun, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.start_run))
            }
        }
    }
}
