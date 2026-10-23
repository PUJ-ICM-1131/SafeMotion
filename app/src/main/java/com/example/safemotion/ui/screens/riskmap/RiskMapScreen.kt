package com.example.safemotion.ui.screens.riskmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.safemotion.R
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.model.RiskEvidence
import com.example.safemotion.data.model.RiskZone
import com.example.safemotion.ui.components.EvidencePlaceholder
import com.example.safemotion.ui.components.RiskCategoryBadge
import com.example.safemotion.ui.components.RiskOsmMap
import com.example.safemotion.ui.components.labelRes
import com.example.safemotion.ui.components.EvidencePhoto

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun RiskMapScreen(
    uiState: RiskMapUiState,
    onZoneSelected: (String) -> Unit,
    onDismissZone: () -> Unit,
    onConfirmZone: (String) -> Unit,
    onReport: () -> Unit,
    onLocationPermissionGranted: () -> Unit,
    onLocationDenied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Basta con que el usuario dé la ubicación precisa o la aproximada.
        if (results.values.any { it }) onLocationPermissionGranted() else onLocationDenied()
    }
    val requestLocation = {
        if (hasLocationPermission(context)) {
            onLocationPermissionGranted()
        } else {
            permissionLauncher.launch(LocationPermissions)
        }
    }

    // Al abrir la pantalla por primera vez se pide la ubicación.
    LaunchedEffect(Unit) {
        if (uiState.userLocation == null && !uiState.locationUnavailable) requestLocation()
    }

    val selectedZone = uiState.selectedZone
    Scaffold(modifier = modifier) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = stringResource(R.string.risk_map_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                RiskOsmMap(
                    zones = uiState.zones,
                    selectedZoneId = uiState.selectedZoneId,
                    onZoneClick = onZoneSelected,
                    modifier = Modifier.fillMaxSize(),
                    userLocation = uiState.userLocation,
                    recenterKey = uiState.locationUpdates
                )
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapSearchBar(
                        isLocating = uiState.isLocating,
                        onLocate = requestLocation
                    )
                    if (uiState.locationUnavailable) {
                        LocationNotice()
                    }
                }
                if (selectedZone == null) {
                    RiskLegend(
                        zones = uiState.zones,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    )
                    ExtendedFloatingActionButton(
                        onClick = onReport,
                        icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                        text = { Text(stringResource(R.string.risk_report_short)) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                    )
                } else {
                    ZoneDetailPanel(
                        zone = selectedZone,
                        confirmed = selectedZone.id in uiState.confirmedZoneIds,
                        onConfirm = { onConfirmZone(selectedZone.id) },
                        onClose = onDismissZone,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    LocationPermissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

@Composable
private fun MapSearchBar(
    isLocating: Boolean,
    onLocate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.risk_search_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (isLocating) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(12.dp).size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onLocate) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.map_locate_me),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(R.string.map_location_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun RiskLegend(
    zones: List<RiskZone>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.risk_legend_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RiskCategory.entries.forEach { category ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskCategoryBadge(category = category, size = 24.dp)
                    Text(
                        text = stringResource(
                            R.string.risk_legend_item,
                            stringResource(category.labelRes()),
                            zones.count { it.category == category }
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneDetailPanel(
    zone: RiskZone,
    confirmed: Boolean,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskCategoryBadge(category = zone.category, size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(zone.category.labelRes()),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = zone.place,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
            Text(
                text = zone.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailItem(icon = Icons.Default.Event, text = zone.reportedAt)
                DetailItem(
                    icon = Icons.Default.Person,
                    text = stringResource(R.string.risk_reported_by, zone.reporter)
                )
            }
            EvidenceRow(evidences = zone.evidences)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onConfirm, enabled = !confirmed) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(
                            if (confirmed) R.string.risk_confirmed else R.string.risk_confirm
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = if (zone.confirmations > 0) {
                        pluralStringResource(
                            R.plurals.risk_confirmations,
                            zone.confirmations,
                            zone.confirmations
                        )
                    } else {
                        stringResource(R.string.risk_no_confirmations)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EvidenceRow(
    evidences: List<RiskEvidence>,
    modifier: Modifier = Modifier
) {
    val visible = evidences.take(2)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        visible.forEachIndexed { index, evidence ->
            val label = stringResource(R.string.risk_evidence_label, index + 1)
            val itemModifier = Modifier.weight(1f).height(120.dp)
            val photoPath = evidence.photoPath
            // Si la evidencia tiene foto real, se muestra; las zonas de ejemplo usan el recuadro.
            if (photoPath != null) {
                EvidencePhoto(
                    path = photoPath,
                    contentDescription = label,
                    modifier = itemModifier
                )
            } else {
                EvidencePlaceholder(label = label, modifier = itemModifier)
            }
        }
        if (visible.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}