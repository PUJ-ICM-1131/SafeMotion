package com.example.safemotion.data.model

enum class RunActivityType { RUNNING, CYCLING }

enum class IncidentType { FALL, INACTIVITY }

data class RunGuardian(val id: String, val name: String)

data class RunSession(
    val activity: RunActivityType,
    val guardianIds: Set<String>,
    val elapsedSeconds: Int = 0,
    val distanceKm: Double = 0.0
)

data class CompletedRun(
    val activity: RunActivityType,
    val durationSeconds: Int,
    val distanceKm: Double,
    val guardianCount: Int,
    val completedAtMillis: Long = System.currentTimeMillis()
)

data class ConfirmedIncident(
    val type: IncidentType,
    val timeMillis: Long,
    val lastLocation: String
)
