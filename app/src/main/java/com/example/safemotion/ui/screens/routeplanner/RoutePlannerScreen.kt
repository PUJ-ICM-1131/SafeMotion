package com.example.safemotion.ui.screens.routeplanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.safemotion.R
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RouteProfile
import com.example.safemotion.ui.components.RiskOsmMap

@Composable
fun RoutePlannerScreen(
    modifier: Modifier = Modifier,
    uiState: RoutePlannerUiState,
    onProfileSelected: (RouteProfile) -> Unit,
    onDestinationSelected: (MapPoint) -> Unit,
    onLoadCurrentLocation: () -> Unit,
    onPermissionDenied: () -> Unit,
    onCalculateRoute: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onLoadCurrentLocation() else onPermissionDenied()
    }

    fun requestCurrentLocation() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation || hasCoarseLocation) {
            onLoadCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        requestCurrentLocation()
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.back))
                }
                Text(
                    text = stringResource(R.string.route_planner_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = stringResource(R.string.route_planner_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.profile == RouteProfile.FOOT,
                    onClick = { onProfileSelected(RouteProfile.FOOT) },
                    label = { Text(stringResource(R.string.route_profile_foot)) }
                )
                FilterChip(
                    selected = uiState.profile == RouteProfile.BIKE,
                    onClick = { onProfileSelected(RouteProfile.BIKE) },
                    label = { Text(stringResource(R.string.route_profile_bike)) }
                )
            }
            RiskOsmMap(
                modifier = Modifier.fillMaxWidth().weight(1f),
                zones = emptyList(),
                selectedZoneId = null,
                onZoneClick = {},
                userLocation = uiState.origin,
                destination = uiState.destination,
                routePoints = uiState.route?.points.orEmpty(),
                destinationLabel = stringResource(R.string.route_destination),
                routeColor = MaterialTheme.colorScheme.primary,
                onMapClick = onDestinationSelected
            )
            RouteSummary(uiState = uiState)
            uiState.error?.let { error ->
                Text(
                    text = stringResource(error.messageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = ::requestCurrentLocation,
                    enabled = !uiState.isLocating,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.route_update_location))
                    }
                }
                Button(
                    onClick = onCalculateRoute,
                    enabled = !uiState.isCalculating,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isCalculating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.route_calculate))
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSummary(
    modifier: Modifier = Modifier,
    uiState: RoutePlannerUiState
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = uiState.origin?.let {
                    stringResource(R.string.route_origin_value, it.latitude, it.longitude)
                } ?: stringResource(R.string.route_waiting_for_location),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = uiState.destination?.let {
                    stringResource(R.string.route_destination_value, it.latitude, it.longitude)
                } ?: stringResource(R.string.route_tap_destination),
                style = MaterialTheme.typography.bodySmall
            )
            uiState.route?.let { route ->
                Text(
                    text = stringResource(
                        R.string.route_summary,
                        route.distanceKilometers,
                        route.durationMinutes
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.route_powered_by_osm_osrm),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@StringRes
private fun RoutePlannerError.messageRes(): Int = when (this) {
    RoutePlannerError.LOCATION_PERMISSION_DENIED -> R.string.route_error_permission
    RoutePlannerError.LOCATION_UNAVAILABLE -> R.string.route_error_location
    RoutePlannerError.ORIGIN_REQUIRED -> R.string.route_error_origin
    RoutePlannerError.DESTINATION_REQUIRED -> R.string.route_error_destination
    RoutePlannerError.NETWORK -> R.string.route_error_network
    RoutePlannerError.NO_ROUTE -> R.string.route_error_no_route
    RoutePlannerError.INVALID_RESPONSE -> R.string.route_error_invalid_response
}
