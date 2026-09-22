package com.example.safemotion.ui.screens.home

data class HomeUiState(
    val firstName: String = "Mariana",
    val acceptedGuardians: Int = 2,
    val lastDistanceKm: String = "6,4",
    val monitoredSessions: Int = 1,
    val receivedAlerts: Int = 1
)
