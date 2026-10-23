package com.example.safemotion.ui.screens.routeplanner

import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.PlannedRoute
import com.example.safemotion.data.model.RouteProfile
import com.example.safemotion.data.repository.RouteException
import com.example.safemotion.data.repository.RouteFailure
import com.example.safemotion.data.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutePlannerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val origin = MapPoint(4.7110, -74.0721)
    private val destination = MapPoint(4.7200, -74.0650)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun currentLocationBecomesRouteOrigin() = runTest {
        val viewModel = createViewModel()

        viewModel.loadCurrentLocation()
        runCurrent()

        assertEquals(origin, viewModel.uiState.value.origin)
        assertFalse(viewModel.uiState.value.isLocating)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun selectedDestinationCanBeRoutedWithBikeProfile() = runTest {
        val repository = RecordingRouteRepository()
        val viewModel = createViewModel(repository)
        viewModel.loadCurrentLocation()
        runCurrent()
        viewModel.selectProfile(RouteProfile.BIKE)
        viewModel.selectDestination(destination)

        viewModel.calculateRoute()
        runCurrent()

        assertEquals(RouteProfile.BIKE, repository.profile)
        assertEquals(origin, repository.origin)
        assertEquals(destination, repository.destination)
        assertEquals(repository.route, viewModel.uiState.value.route)
        assertFalse(viewModel.uiState.value.isCalculating)
    }

    @Test
    fun selectingAnotherDestinationClearsPreviousRoute() = runTest {
        val viewModel = createViewModel()
        viewModel.loadCurrentLocation()
        runCurrent()
        viewModel.selectDestination(destination)
        viewModel.calculateRoute()
        runCurrent()
        assertTrue(viewModel.uiState.value.route != null)

        viewModel.selectDestination(MapPoint(4.73, -74.06))

        assertNull(viewModel.uiState.value.route)
    }

    @Test
    fun calculationWithoutDestinationShowsDestinationRequired() = runTest {
        val viewModel = createViewModel()
        viewModel.loadCurrentLocation()
        runCurrent()

        viewModel.calculateRoute()

        assertEquals(RoutePlannerError.DESTINATION_REQUIRED, viewModel.uiState.value.error)
    }

    @Test
    fun noRouteFailureIsExposedToTheScreen() = runTest {
        val repository = RecordingRouteRepository(
            result = Result.failure(RouteException(RouteFailure.NO_ROUTE))
        )
        val viewModel = createViewModel(repository)
        viewModel.loadCurrentLocation()
        runCurrent()
        viewModel.selectDestination(destination)

        viewModel.calculateRoute()
        runCurrent()

        assertEquals(RoutePlannerError.NO_ROUTE, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.route)
    }

    private fun createViewModel(
        repository: RecordingRouteRepository = RecordingRouteRepository()
    ) = RoutePlannerViewModel(
        routeRepository = repository,
        currentLocation = { origin }
    )

    private class RecordingRouteRepository(
        val route: PlannedRoute = PlannedRoute(
            points = listOf(
                MapPoint(4.7110, -74.0721),
                MapPoint(4.7200, -74.0650)
            ),
            distanceMeters = 1_250.0,
            durationSeconds = 780.0
        ),
        private val result: Result<PlannedRoute> = Result.success(route)
    ) : RouteRepository {
        var origin: MapPoint? = null
        var destination: MapPoint? = null
        var profile: RouteProfile? = null

        override suspend fun calculateRoute(
            origin: MapPoint,
            destination: MapPoint,
            profile: RouteProfile
        ): Result<PlannedRoute> {
            this.origin = origin
            this.destination = destination
            this.profile = profile
            return result
        }
    }
}
