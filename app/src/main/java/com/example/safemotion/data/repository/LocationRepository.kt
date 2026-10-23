package com.example.safemotion.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.example.safemotion.data.model.MapPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationRepository(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

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
}