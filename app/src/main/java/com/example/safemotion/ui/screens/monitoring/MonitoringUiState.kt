package com.example.safemotion.ui.screens.monitoring

import com.example.safemotion.data.model.MonitoredSession

data class MonitoringUiState(
    val session: MonitoredSession? = null,
    val isMissing: Boolean = false
)
