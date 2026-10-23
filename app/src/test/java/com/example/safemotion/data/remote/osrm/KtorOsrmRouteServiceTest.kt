package com.example.safemotion.data.remote.osrm

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorOsrmRouteServiceTest {
    @Test
    fun coordinateSeparatorIsEncodedInTheOsrmRequestPath() = runTest {
        var encodedPath = ""
        val engine = MockEngine { request ->
            encodedPath = request.url.encodedPath
            respond(
                content = VALID_RESPONSE,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = KtorOsrmRouteService(client)

        val response = service.getRoute(
            baseUrl = "https://routing.openstreetmap.de/routed-foot",
            coordinatePath = "-74.0649,4.6285;-74.0672,4.6492"
        )

        assertEquals("Ok", response.code)
        assertEquals(
            "/routed-foot/route/v1/driving/-74.0649,4.6285;-74.0672,4.6492",
            encodedPath
        )
    }

    private companion object {
        const val VALID_RESPONSE = """
            {
              "code": "Ok",
              "routes": [
                {
                  "distance": 1200.0,
                  "duration": 600.0,
                  "geometry": {
                    "coordinates": [[-74.0649, 4.6285], [-74.0672, 4.6492]]
                  }
                }
              ]
            }
        """
    }
}
