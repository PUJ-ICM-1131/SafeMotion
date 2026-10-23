package com.example.safemotion.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.safemotion.data.model.MapPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepository(context: Context) {
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
