package com.example.safemotion.ui.screens.run

import com.example.safemotion.data.model.IncidentType
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.model.RunGuardian
import com.example.safemotion.data.model.RunSession
import com.example.safemotion.data.sensor.SensorStatus

enum class RunPhase { PREPARING, ACTIVE, ALERT }

data class RunUiState(
    val phase: RunPhase = RunPhase.PREPARING,
    val activity: RunActivityType = RunActivityType.RUNNING,
    val guardians: List<RunGuardian> = emptyList(),
    val guardiansLoaded: Boolean = false,
    val selectedGuardianIds: Set<String> = emptySet(),
    val needsGuardian: Boolean = false,
    val session: RunSession? = null,
    val sessionId: String? = null,
    val isStarting: Boolean = false,
    val startFailed: Boolean = false,
    val locationDenied: Boolean = false,
    val sensorStatus: SensorStatus = SensorStatus(),
    val incidentType: IncidentType? = null,
    val secondsToCancel: Int = 30,
    val alertSent: Boolean = false
)
