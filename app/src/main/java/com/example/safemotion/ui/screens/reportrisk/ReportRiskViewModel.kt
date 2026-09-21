package com.example.safemotion.ui.screens.reportrisk

import androidx.lifecycle.ViewModel
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.repository.MockRiskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReportRiskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportRiskUiState())
    val uiState = _uiState.asStateFlow()

    fun selectCategory(category: RiskCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun changeDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun useCurrentLocation() {
        _uiState.update { it.copy(location = MockRiskRepository.CURRENT_LOCATION) }
    }

    fun attachPhoto(source: MockPhotoSource) {
        _uiState.update { it.copy(photoSource = source) }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoSource = null) }
    }

    fun submit(): Boolean {
        val state = _uiState.value
        val category = state.category
        if (state.sent || !state.isComplete || category == null) return false
        MockRiskRepository.addReport(
            category = category,
            description = state.description.trim(),
            place = state.location
        )
        _uiState.update { it.copy(sent = true) }
        return true
    }

    fun reset() {
        _uiState.value = ReportRiskUiState()
    }

    // Si el reporte anterior ya se envió, el formulario vuelve a empezar vacío.
    fun prepareNewReport() {
        if (_uiState.value.sent) reset()
    }
}