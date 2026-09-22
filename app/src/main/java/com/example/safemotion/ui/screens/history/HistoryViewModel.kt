package com.example.safemotion.ui.screens.history

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.model.CompletedRun
import com.example.safemotion.data.model.RunActivityType
import com.example.safemotion.data.repository.MockRunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(private val includeSamples: Boolean = true) : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = buildState()
    }

    private fun buildState(): HistoryUiState {
        val completed = MockRunRepository.completedRuns.value.mapIndexed { index, run ->
            run.toHistoryItem("completed-$index")
        }
        val runs = completed + if (includeSamples) sampleRuns else emptyList()
        return HistoryUiState(runs = runs, isEmpty = runs.isEmpty())
    }

    private fun CompletedRun.toHistoryItem(id: String) = HistoryRunItem(
        id = id,
        activity = activity,
        completedAtMillis = completedAtMillis,
        durationSeconds = durationSeconds,
        distanceKm = distanceKm,
        status = HistoryRunStatus.COMPLETED
    )

    private companion object {
        private val referenceTime = System.currentTimeMillis()
        private val sampleRuns = listOf(
            HistoryRunItem(
                id = "sample-1",
                activity = RunActivityType.RUNNING,
                completedAtMillis = referenceTime - 24 * 60 * 60 * 1_000L,
                durationSeconds = 38 * 60,
                distanceKm = 6.4,
                status = HistoryRunStatus.COMPLETED
            ),
            HistoryRunItem(
                id = "sample-2",
                activity = RunActivityType.RUNNING,
                completedAtMillis = referenceTime - 6 * 24 * 60 * 60 * 1_000L,
                durationSeconds = 24 * 60,
                distanceKm = 4.3,
                status = HistoryRunStatus.ALERT_SENT
            ),
            HistoryRunItem(
                id = "sample-3",
                activity = RunActivityType.CYCLING,
                completedAtMillis = referenceTime - 9 * 24 * 60 * 60 * 1_000L,
                durationSeconds = 72 * 60,
                distanceKm = 24.8,
                status = HistoryRunStatus.COMPLETED
            ),
            HistoryRunItem(
                id = "sample-4",
                activity = RunActivityType.RUNNING,
                completedAtMillis = referenceTime - 11 * 24 * 60 * 60 * 1_000L,
                durationSeconds = 31 * 60,
                distanceKm = 5.1,
                status = HistoryRunStatus.INTERRUPTED
            )
        )
    }
}
