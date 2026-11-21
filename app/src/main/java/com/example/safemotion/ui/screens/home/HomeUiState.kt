package com.example.safemotion.ui.screens.home

data class HomeUiState(
    val firstName: String = "Mariana",
    val acceptedGuardians: Int = 0,
    val lastDistanceKm: String = "6,4",
    val monitoredSessions: Int = 0,
    val receivedAlerts: Int = 0
)
