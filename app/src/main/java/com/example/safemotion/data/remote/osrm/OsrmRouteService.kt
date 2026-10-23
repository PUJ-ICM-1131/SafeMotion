package com.example.safemotion.data.remote.osrm

interface OsrmRouteService {
    suspend fun getRoute(baseUrl: String, coordinatePath: String): OsrmResponseDto
}
