package com.example.safemotion.data.repository

import com.example.safemotion.data.model.MapPoint
import kotlinx.coroutines.flow.Flow

interface PositionTracker {
    fun positions(): Flow<MapPoint>
    fun distanceMeters(from: MapPoint, to: MapPoint): Float
}