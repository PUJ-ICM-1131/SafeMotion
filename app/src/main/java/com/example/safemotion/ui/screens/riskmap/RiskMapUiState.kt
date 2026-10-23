package com.example.safemotion.ui.screens.riskmap

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RiskZone

data class RiskMapUiState(
    val zones: List<RiskZone> = emptyList(),
    val confirmedZoneIds: Set<String> = emptySet(),
    val selectedZoneId: String? = null,
    val userLocation: MapPoint? = null,
    val isLocating: Boolean = false,
    val locationUnavailable: Boolean = false
) {
    val selectedZone: RiskZone?
        get() = zones.firstOrNull { it.id == selectedZoneId }
}