package com.example.safemotion.ui.screens.reportrisk

import com.example.safemotion.data.model.RiskCategory

// De dónde salió la foto de evidencia.
enum class PhotoSource { CAMERA, GALLERY }

data class ReportRiskUiState(
    val category: RiskCategory? = null,
    val description: String = "",
    val location: String = "",
    // Coordenadas del GPS; se llenan junto con la dirección al usar la ubicación actual.
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocating: Boolean = false,
    val locationUnavailable: Boolean = false,
    // Ruta de la foto en el almacenamiento interno de la app.
    val photoPath: String? = null,
    val photoSource: PhotoSource? = null,
    val isSavingPhoto: Boolean = false,
    val photoFailed: Boolean = false,
    val cameraDenied: Boolean = false,
    val sent: Boolean = false
) {
    val hasPhoto: Boolean
        get() = photoPath != null

    // El reporte solo se puede enviar con categoría, descripción, ubicación y foto.
    val isComplete: Boolean
        get() = category != null &&
                description.isNotBlank() &&
                location.isNotBlank() &&
                hasPhoto

    // Se muestra el error cuando ya está todo lo demás y solo falta la foto.
    val photoMissing: Boolean
        get() = !hasPhoto &&
                category != null &&
                description.isNotBlank() &&
                location.isNotBlank()
}