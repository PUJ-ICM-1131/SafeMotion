package com.example.safemotion.ui.screens.routeplanner

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.PlannedRoute
import com.example.safemotion.data.model.RouteProfile

enum class RoutePlannerError {
    LOCATION_PERMISSION_DENIED,
    LOCATION_UNAVAILABLE,
    ORIGIN_REQUIRED,
    DESTINATION_REQUIRED,
    NETWORK,
    NO_ROUTE,
    INVALID_RESPONSE
}

data class RoutePlannerUiState(
    val profile: RouteProfile = RouteProfile.FOOT,
    val origin: MapPoint? = null,
    val destination: MapPoint? = null,
    val route: PlannedRoute? = null,
    val isLocating: Boolean = false,
    val isCalculating: Boolean = false,
    val error: RoutePlannerError? = null
)
