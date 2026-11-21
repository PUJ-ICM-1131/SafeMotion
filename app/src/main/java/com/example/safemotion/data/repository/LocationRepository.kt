package com.example.safemotion.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.example.safemotion.data.model.MapPoint
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepository(context: Context) : PositionTracker {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    // La pantalla verifica y pide el permiso de ubicación antes de llamar este método.
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): MapPoint? = try {
        val fresh = client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        val location = fresh ?: client.lastLocation.await()
        location?.let { MapPoint(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null
    }

    // Actualizaciones continuas del GPS: cada 5 s (o cada 3 s si otra app ya las pidió).
    // callbackFlow convierte el LocationCallback en un Flow; al dejar de recolectarlo,
    // awaitClose quita el callback para que el GPS no siga encendido.
    @SuppressLint("MissingPermission")
    override fun positions(): Flow<MapPoint> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(3_000L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(MapPoint(it.latitude, it.longitude)) }
            }
        }
        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close(e)
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    override fun distanceMeters(from: MapPoint, to: MapPoint): Float {
        val result = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, result)
        return result[0]
    }

    // Geocodificación inversa: convierte las coordenadas en una dirección legible.
    suspend fun addressFor(point: MapPoint): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(appContext, Locale.getDefault())
        return try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Desde Android 13 el Geocoder responde con un callback, que se envuelve en una corrutina.
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        point.latitude,
                        point.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                continuation.resume(addresses)
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(emptyList())
                            }
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(point.latitude, point.longitude, 1).orEmpty()
                }
            }
            addresses.firstOrNull()?.getAddressLine(0)
        } catch (e: IOException) {
            null
        }
    }
}