package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.PlannedRoute
import com.example.safemotion.data.model.RouteProfile
import com.example.safemotion.data.remote.osrm.OsrmResponseDto
import com.example.safemotion.data.remote.osrm.OsrmRouteService

interface RouteRepository {
    suspend fun calculateRoute(
        origin: MapPoint,
        destination: MapPoint,
        profile: RouteProfile
    ): Result<PlannedRoute>
}

enum class RouteFailure {
    NETWORK,
    NO_ROUTE,
    INVALID_RESPONSE
}

class RouteException(
    val failure: RouteFailure,
    cause: Throwable? = null
) : Exception(failure.name, cause)

class OsrmRouteRepository(
    private val service: OsrmRouteService
) : RouteRepository {
    override suspend fun calculateRoute(
        origin: MapPoint,
        destination: MapPoint,
        profile: RouteProfile
    ): Result<PlannedRoute> = runCatching {
        val response = try {
            service.getRoute(
                baseUrl = profile.baseUrl,
                coordinatePath = "${origin.longitude},${origin.latitude};" +
                    "${destination.longitude},${destination.latitude}"
            )
        } catch (error: RouteException) {
            throw error
        } catch (error: Exception) {
            throw RouteException(RouteFailure.NETWORK, error)
        }
        response.toPlannedRoute()
    }
}

private val RouteProfile.baseUrl: String
    get() = when (this) {
        RouteProfile.FOOT -> "https://routing.openstreetmap.de/routed-foot"
        RouteProfile.BIKE -> "https://routing.openstreetmap.de/routed-bike"
    }

private fun OsrmResponseDto.toPlannedRoute(): PlannedRoute {
    if (code != "Ok" || routes.isEmpty()) {
        throw RouteException(RouteFailure.NO_ROUTE)
    }
    val route = routes.first()
    val points = route.geometry.coordinates.map { coordinate ->
        if (coordinate.size < 2) {
            throw RouteException(RouteFailure.INVALID_RESPONSE)
        }
        try {
            MapPoint(
                latitude = coordinate[1],
                longitude = coordinate[0]
            )
        } catch (error: IllegalArgumentException) {
            throw RouteException(RouteFailure.INVALID_RESPONSE, error)
        }
    }
    if (points.size < 2) {
        throw RouteException(RouteFailure.INVALID_RESPONSE)
    }
    return PlannedRoute(
        points = points,
        distanceMeters = route.distance,
        durationSeconds = route.duration
    )
}
