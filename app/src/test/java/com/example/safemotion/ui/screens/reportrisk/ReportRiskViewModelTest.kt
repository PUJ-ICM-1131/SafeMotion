package com.example.safemotion.ui.screens.reportrisk

import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.repository.MockRiskRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportRiskViewModelTest {
    private fun ReportRiskViewModel.fillTextFields() {
        selectCategory(RiskCategory.INSECURITY)
        changeDescription("Dos motos rondando el sendero sobre las 5:30 a. m.")
        useCurrentLocation()
    }

    @Test
    fun reportCannotBeSentWithoutPhoto() {
        val viewModel = ReportRiskViewModel()
        viewModel.fillTextFields()
        val zonesBefore = MockRiskRepository.zones.value.size

        assertTrue(viewModel.uiState.value.photoMissing)
        assertFalse(viewModel.uiState.value.isComplete)
        assertFalse(viewModel.submit())
        assertEquals(zonesBefore, MockRiskRepository.zones.value.size)
    }

    @Test
    fun reportWithPhotoIsSentOnlyOnce() {
        val viewModel = ReportRiskViewModel()
        viewModel.fillTextFields()
        viewModel.attachPhoto(MockPhotoSource.CAMERA)
        val zonesBefore = MockRiskRepository.zones.value.size

        assertFalse(viewModel.uiState.value.photoMissing)
        assertTrue(viewModel.uiState.value.isComplete)
        assertTrue(viewModel.submit())
        assertTrue(viewModel.uiState.value.sent)
        assertFalse(viewModel.submit())
        assertEquals(zonesBefore + 1, MockRiskRepository.zones.value.size)
        assertEquals(RiskCategory.INSECURITY, MockRiskRepository.zones.value.last().category)
    }

    @Test
    fun removingThePhotoMakesTheReportIncompleteAgain() {
        val viewModel = ReportRiskViewModel()
        viewModel.fillTextFields()
        viewModel.attachPhoto(MockPhotoSource.GALLERY)
        assertTrue(viewModel.uiState.value.isComplete)

        viewModel.removePhoto()
        assertFalse(viewModel.uiState.value.isComplete)
        assertTrue(viewModel.uiState.value.photoMissing)
    }

    @Test
    fun blankDescriptionKeepsTheReportIncomplete() {
        val viewModel = ReportRiskViewModel()
        viewModel.fillTextFields()
        viewModel.attachPhoto(MockPhotoSource.CAMERA)
        viewModel.changeDescription("   ")

        assertFalse(viewModel.uiState.value.isComplete)
        assertFalse(viewModel.submit())
    }

    @Test
    fun prepareNewReportClearsTheFormOnlyAfterSending() {
        val viewModel = ReportRiskViewModel()
        viewModel.fillTextFields()
        viewModel.prepareNewReport()
        assertEquals(RiskCategory.INSECURITY, viewModel.uiState.value.category)

        viewModel.attachPhoto(MockPhotoSource.CAMERA)
        assertTrue(viewModel.submit())
        viewModel.prepareNewReport()
        assertEquals(ReportRiskUiState(), viewModel.uiState.value)
    }
}