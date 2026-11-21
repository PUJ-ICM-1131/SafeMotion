package com.example.safemotion.ui.screens.receivedalert

import com.example.safemotion.data.model.MonitoredSession
import com.example.safemotion.data.model.ReceivedAlert

data class ReceivedAlertUiState(
    val alert: ReceivedAlert? = null,
    // Recorrido al que pertenece la alerta, para ver el mapa y la actividad.
    val session: MonitoredSession? = null,
    val address: String? = null,
    val isLoading: Boolean = true,
    val isMissing: Boolean = false
) {
    val monitoredSessionId: String?
        get() = session?.id
}
