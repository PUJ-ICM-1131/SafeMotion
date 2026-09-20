package com.example.safemotion.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.repository.MockAuthRepository
import com.example.safemotion.data.repository.MockRunRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(firstName = MockAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana")
    )
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        val lastDistance = MockRunRepository.completedRuns.value.firstOrNull()?.distanceKm
        _uiState.value = HomeUiState(
            firstName = MockAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana",
            lastDistanceKm = lastDistance?.let {
                String.format(Locale.getDefault(), "%.1f", it)
            } ?: "6,4"
        )
    }
}
