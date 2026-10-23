package com.example.safemotion.data.remote.osrm

import com.example.safemotion.data.repository.RouteException
import com.example.safemotion.data.repository.RouteFailure
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorOsrmRouteService(
    private val client: HttpClient = defaultClient()
) : OsrmRouteService {
    override suspend fun getRoute(baseUrl: String, coordinatePath: String): OsrmResponseDto {
        val response = client.get("$baseUrl/route/v1/driving/$coordinatePath") {
            parameter("overview", "full")
            parameter("geometries", "geojson")
            parameter("steps", "false")
        }
        if (!response.status.isSuccess()) {
            throw RouteException(RouteFailure.NETWORK)
        }
        return response.body()
    }

    private companion object {
        fun defaultClient() = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}
