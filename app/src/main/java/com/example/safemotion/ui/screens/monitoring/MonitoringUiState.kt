package com.example.safemotion.ui.screens.monitoring

import com.example.safemotion.data.model.MonitoredSession

data class MonitoringUiState(
    val session: MonitoredSession? = null,
    val isLoading: Boolean = true,
    val isMissing: Boolean = false,
    // Dirección de la última posición del corredor (Geocoder); null mientras se calcula.
    val address: String? = null,
    // Hora actual, para calcular hace cuánto llegó la última posición.
    val nowMillis: Long = System.currentTimeMillis()
)
