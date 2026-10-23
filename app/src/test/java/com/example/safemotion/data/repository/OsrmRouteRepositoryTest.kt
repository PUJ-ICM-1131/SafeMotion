package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RouteProfile
import com.example.safemotion.data.remote.osrm.OsrmGeometryDto
import com.example.safemotion.data.remote.osrm.OsrmResponseDto
import com.example.safemotion.data.remote.osrm.OsrmRouteDto
import com.example.safemotion.data.remote.osrm.OsrmRouteService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OsrmRouteRepositoryTest {
    @Test
    fun footProfileUsesFootServerAndMapsGeoJsonLongitudeLatitudeOrder() = runTest {
        val service = RecordingRouteService(
            response = OsrmResponseDto(
                code = "Ok",
                routes = listOf(
                    OsrmRouteDto(
                        distance = 1_250.0,
                        duration = 780.0,
                        geometry = OsrmGeometryDto(
                            coordinates = listOf(
                                listOf(-74.0721, 4.7110),
                                listOf(-74.0650, 4.7200)
                            )
                        )
                    )
                )
            )
        )
        val repository = OsrmRouteRepository(service)

        val result = repository.calculateRoute(
            origin = MapPoint(latitude = 4.7110, longitude = -74.0721),
            destination = MapPoint(latitude = 4.7200, longitude = -74.0650),
            profile = RouteProfile.FOOT
        )

        assertTrue(result.isSuccess)
        assertEquals("https://routing.openstreetmap.de/routed-foot", service.baseUrl)
        assertEquals("-74.0721,4.711;-74.065,4.72", service.coordinatePath)
        assertEquals(
            MapPoint(latitude = 4.7110, longitude = -74.0721),
            result.getOrThrow().points.first()
        )
        assertEquals(1_250.0, result.getOrThrow().distanceMeters, 0.0)
        assertEquals(780.0, result.getOrThrow().durationSeconds, 0.0)
    }

    @Test
    fun bikeProfileUsesBikeServer() = runTest {
        val service = RecordingRouteService(validResponse())
        val repository = OsrmRouteRepository(service)

        repository.calculateRoute(
            origin = MapPoint(4.70, -74.08),
            destination = MapPoint(4.71, -74.07),
            profile = RouteProfile.BIKE
        ).getOrThrow()

        assertEquals("https://routing.openstreetmap.de/routed-bike", service.baseUrl)
    }

    @Test
    fun responseWithoutRoutesReturnsNoRouteFailure() = runTest {
        val service = RecordingRouteService(
            OsrmResponseDto(code = "NoRoute", routes = emptyList(), message = "No route found")
        )
        val repository = OsrmRouteRepository(service)

        val result = repository.calculateRoute(
            origin = MapPoint(4.70, -74.08),
            destination = MapPoint(4.71, -74.07),
            profile = RouteProfile.FOOT
        )

        assertTrue(result.isFailure)
        assertEquals(RouteFailure.NO_ROUTE, (result.exceptionOrNull() as RouteException).failure)
    }

    private class RecordingRouteService(
        private val response: OsrmResponseDto
    ) : OsrmRouteService {
        var baseUrl: String? = null
        var coordinatePath: String? = null

        override suspend fun getRoute(baseUrl: String, coordinatePath: String): OsrmResponseDto {
            this.baseUrl = baseUrl
            this.coordinatePath = coordinatePath
            return response
        }
    }

    private companion object {
        fun validResponse() = OsrmResponseDto(
            code = "Ok",
            routes = listOf(
                OsrmRouteDto(
                    distance = 10.0,
                    duration = 5.0,
                    geometry = OsrmGeometryDto(
                        coordinates = listOf(
                            listOf(-74.08, 4.70),
                            listOf(-74.07, 4.71)
                        )
                    )
                )
            )
        )
    }
}
