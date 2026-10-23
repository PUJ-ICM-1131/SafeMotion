package com.example.safemotion.data.remote.osrm

import kotlinx.serialization.Serializable

@Serializable
data class OsrmResponseDto(
    val code: String,
    val routes: List<OsrmRouteDto> = emptyList(),
    val message: String? = null
)

@Serializable
data class OsrmRouteDto(
    val distance: Double,
    val duration: Double,
    val geometry: OsrmGeometryDto
)

@Serializable
data class OsrmGeometryDto(
    val coordinates: List<List<Double>>
)
