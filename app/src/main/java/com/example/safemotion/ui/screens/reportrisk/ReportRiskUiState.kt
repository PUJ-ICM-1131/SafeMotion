package com.example.safemotion.ui.screens.reportrisk

import com.example.safemotion.data.model.RiskCategory

// La cámara y la galería se simulan: solo se guarda de dónde "vino" la foto.
enum class MockPhotoSource { CAMERA, GALLERY }

data class ReportRiskUiState(
    val category: RiskCategory? = null,
    val description: String = "",
    val location: String = "",
    val photoSource: MockPhotoSource? = null,
    val sent: Boolean = false
) {
    val hasPhoto: Boolean
        get() = photoSource != null

    // El reporte solo se puede enviar con categoría, descripción, ubicación y foto.
    val isComplete: Boolean
        get() = category != null &&
                description.isNotBlank() &&
                location.isNotBlank() &&
                hasPhoto

    // Se muestra el error cuando ya está todo y solo falta la foto.
    val photoMissing: Boolean
        get() = !hasPhoto &&
                category != null &&
                description.isNotBlank() &&
                location.isNotBlank()
}