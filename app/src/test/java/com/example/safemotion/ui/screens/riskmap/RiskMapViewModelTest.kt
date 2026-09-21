package com.example.safemotion.ui.screens.riskmap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskMapViewModelTest {
    @Test
    fun selectingTheSameZoneTwiceClosesItsDetail() {
        val viewModel = RiskMapViewModel()

        viewModel.selectZone("z1")
        assertEquals("z1", viewModel.uiState.value.selectedZone?.id)
        viewModel.selectZone("z1")
        assertNull(viewModel.uiState.value.selectedZone)
    }

    @Test
    fun unknownZoneIsIgnored() {
        val viewModel = RiskMapViewModel()

        viewModel.selectZone("no-existe")
        assertNull(viewModel.uiState.value.selectedZoneId)
    }

    @Test
    fun aZoneCanBeConfirmedOnlyOnce() {
        val viewModel = RiskMapViewModel()
        val before = viewModel.uiState.value.zones.first { it.id == "z2" }.confirmations

        viewModel.confirmZone("z2")
        viewModel.confirmZone("z2")

        val after = viewModel.uiState.value.zones.first { it.id == "z2" }.confirmations
        assertEquals(before + 1, after)
        assertTrue("z2" in viewModel.uiState.value.confirmedZoneIds)
    }
}