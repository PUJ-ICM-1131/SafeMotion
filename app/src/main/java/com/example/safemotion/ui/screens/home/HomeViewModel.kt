package com.example.safemotion.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.repository.MockAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(firstName = MockAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana")
    )
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = HomeUiState(
            firstName = MockAuthRepository.currentUser?.name?.substringBefore(" ") ?: "Mariana"
        )
    }
}
