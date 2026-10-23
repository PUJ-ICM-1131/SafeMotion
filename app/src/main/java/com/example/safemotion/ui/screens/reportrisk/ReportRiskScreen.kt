package com.example.safemotion.ui.screens.reportrisk

import android.Manifest
import android.net.Uri
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.ui.components.EvidencePhoto
import com.example.safemotion.ui.components.icon
import com.example.safemotion.ui.components.labelRes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

private val LocationPermissions = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportRiskScreen(
    modifier: Modifier = Modifier,
    uiState: ReportRiskUiState,
    onCategorySelected: (RiskCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onLocationDenied: () -> Unit,
    onPrepareCameraPhoto: () -> Uri,
    onCameraResult: (Boolean) -> Unit,
    onCameraDenied: () -> Unit,
    onGalleryPhotoPicked: (Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    onSubmit: () -> Boolean,
    onReportSent: () -> Unit,
    onClose: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sentMessage = stringResource(R.string.report_sent)
    val viewLabel = stringResource(R.string.report_view)

    // Cámara: la app de cámara del sistema escribe la foto en la Uri que le damos.
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }

    // Permiso de cámara en tiempo de ejecución con Accompanist.
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) cameraLauncher.launch(onPrepareCameraPhoto()) else onCameraDenied()
    }

    // Permiso de ubicación: basta con la precisa o la aproximada.
    val locationPermissions = rememberMultiplePermissionsState(LocationPermissions) { results ->
        if (results.values.any { it }) onUseCurrentLocation() else onLocationDenied()
    }

    // Galería: selector de fotos del sistema, no necesita permiso.
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onGalleryPhotoPicked) }

    val takePhoto = {
        if (cameraPermission.status.isGranted) {
            cameraLauncher.launch(onPrepareCameraPhoto())
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }
    val requestLocation = {
        if (locationPermissions.permissions.any { it.status.isGranted }) {
            onUseCurrentLocation()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }
    val pickFromGallery = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_risk_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (onSubmit()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = sentMessage,
                                        actionLabel = viewLabel,
                                        duration = SnackbarDuration.Short
                                    )
                                    onReportSent()
                                }
                            }
                        },
                        enabled = uiState.isComplete && !uiState.sent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Text(
                            text = stringResource(R.string.report_send),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (!uiState.isComplete) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.report_incomplete_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CategorySection(
                selected = uiState.category,
                onCategorySelected = onCategorySelected
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.report_description)) },
                placeholder = { Text(stringResource(R.string.report_description_hint)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            LocationSection(
                location = uiState.location,
                isLocating = uiState.isLocating,
                locationUnavailable = uiState.locationUnavailable,
                onUseCurrentLocation = requestLocation
            )
            PhotoSection(
                uiState = uiState,
                onTakePhoto = takePhoto,
                onPickFromGallery = pickFromGallery,
                onRemovePhoto = onRemovePhoto
            )
        }
    }
}

@Composable
private fun CategorySection(
    modifier: Modifier = Modifier,
    selected: RiskCategory?,
    onCategorySelected: (RiskCategory) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.report_category),
            style = MaterialTheme.typography.titleMedium
        )
        RiskCategory.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { category ->
                    FilterChip(
                        selected = category == selected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(stringResource(category.labelRes())) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (category == selected) {
                                    Icons.Default.Check
                                } else {
                                    category.icon()
                                },
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    modifier: Modifier = Modifier,
    location: String,
    isLocating: Boolean,
    locationUnavailable: Boolean,
    onUseCurrentLocation: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.report_location),
            style = MaterialTheme.typography.titleMedium
        )
        if (location.isEmpty()) {
            OutlinedButton(
                onClick = onUseCurrentLocation,
                enabled = !isLocating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.report_use_current_location),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (locationUnavailable) {
                Text(
                    text = stringResource(R.string.map_location_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSection(
    modifier: Modifier = Modifier,
    uiState: ReportRiskUiState,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val photoPath = uiState.photoPath
    val hasError = uiState.photoMissing
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.report_photo_required),
            style = MaterialTheme.typography.titleMedium,
            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        if (photoPath != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                EvidencePhoto(
                    path = photoPath,
                    contentDescription = stringResource(R.string.report_photo_description),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                IconButton(
                    onClick = onRemovePhoto,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.report_remove_photo)
                    )
                }
            }
            Text(
                text = stringResource(
                    if (uiState.photoSource == PhotoSource.GALLERY) {
                        R.string.report_photo_from_gallery
                    } else {
                        R.string.report_photo_from_camera
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth().height(128.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (hasError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isSavingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = if (hasError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = stringResource(R.string.report_no_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            when {
                uiState.cameraDenied -> PhotoMessage(messageRes = R.string.report_camera_denied)
                uiState.photoFailed -> PhotoMessage(messageRes = R.string.report_photo_save_failed)
                hasError -> PhotoMessage(messageRes = R.string.report_photo_error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onTakePhoto,
                    enabled = !uiState.isSavingPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.report_take_photo),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                OutlinedButton(
                    onClick = onPickFromGallery,
                    enabled = !uiState.isSavingPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.report_gallery),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoMessage(
    modifier: Modifier = Modifier,
    @StringRes messageRes: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
