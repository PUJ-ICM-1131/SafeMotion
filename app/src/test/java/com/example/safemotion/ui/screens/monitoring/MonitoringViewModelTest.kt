package com.example.safemotion.ui.screens.monitoring

import com.example.safemotion.data.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitoringViewModelTest {
    @Test
    fun selectsRequestedMonitoredSession() {
        val viewModel = MonitoringViewModel()

        assertEquals("session-laura", viewModel.uiState.value.session?.id)
        assertEquals(TrackingStatus.ACTIVE, viewModel.uiState.value.session?.status)

        viewModel.selectSession("session-camilo")

        assertEquals(TrackingStatus.INTERRUPTED, viewModel.uiState.value.session?.status)
        assertTrue(!viewModel.uiState.value.isMissing)
    }

    @Test
    fun missingSessionProducesRecoverableState() {
        val viewModel = MonitoringViewModel("missing")

        assertNull(viewModel.uiState.value.session)
        assertTrue(viewModel.uiState.value.isMissing)
    }
}
