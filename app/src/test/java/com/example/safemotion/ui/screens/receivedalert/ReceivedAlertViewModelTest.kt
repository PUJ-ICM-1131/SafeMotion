package com.example.safemotion.ui.screens.receivedalert

import com.example.safemotion.data.model.IncidentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceivedAlertViewModelTest {
    @Test
    fun exposesAlertAndMatchingSession() {
        val viewModel = ReceivedAlertViewModel()

        assertEquals(IncidentType.FALL, viewModel.uiState.value.alert?.incidentType)
        assertEquals("session-camilo", viewModel.uiState.value.monitoredSessionId)
        assertTrue(!viewModel.uiState.value.isMissing)
    }

    @Test
    fun missingAlertCannotOpenMonitoring() {
        val viewModel = ReceivedAlertViewModel("missing")

        assertNull(viewModel.uiState.value.alert)
        assertNull(viewModel.uiState.value.monitoredSessionId)
        assertTrue(viewModel.uiState.value.isMissing)
    }
}
