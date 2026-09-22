package com.example.safemotion.ui.screens.receivedalert

import com.example.safemotion.data.model.ReceivedAlert

data class ReceivedAlertUiState(
    val alert: ReceivedAlert? = null,
    val monitoredSessionId: String? = null,
    val isMissing: Boolean = false
)
