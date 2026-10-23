package com.example.safemotion.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RouteProfile {
    FOOT,
    BIKE
}

data class PlannedRoute(
    val points: List<MapPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
) {
    val distanceKilometers: Double
        get() = distanceMeters / 1_000.0

    val durationMinutes: Int
        get() = kotlin.math.ceil(durationSeconds / 60.0).toInt()

    init {
        require(points.size >= 2) { "A route needs at least two points" }
        require(distanceMeters >= 0.0) { "Distance cannot be negative" }
        require(durationSeconds >= 0.0) { "Duration cannot be negative" }
    }
}
