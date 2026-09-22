package com.example.safemotion.ui.screens.history

import com.example.safemotion.data.model.RunActivityType

enum class HistoryRunStatus { COMPLETED, ALERT_SENT, INTERRUPTED }

data class HistoryRunItem(
    val id: String,
    val activity: RunActivityType,
    val completedAtMillis: Long,
    val durationSeconds: Int,
    val distanceKm: Double,
    val status: HistoryRunStatus
)

data class HistoryUiState(
    val runs: List<HistoryRunItem> = emptyList(),
    val isEmpty: Boolean = true
)
