package com.example.safemotion.data.model

enum class RunActivityType { RUNNING, CYCLING }

enum class IncidentType { FALL, INACTIVITY }

data class RunGuardian(val id: Int, val name: String)

data class RunSession(
    val activity: RunActivityType,
    val guardianIds: Set<Int>,
    val elapsedSeconds: Int = 0,
    val distanceKm: Double = 0.0
)

data class CompletedRun(
    val activity: RunActivityType,
    val durationSeconds: Int,
    val distanceKm: Double,
    val guardianCount: Int
)

data class ConfirmedIncident(
    val type: IncidentType,
    val timeMillis: Long,
    val lastLocation: String
)
