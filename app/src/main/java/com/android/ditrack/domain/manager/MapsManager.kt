package com.android.ditrack.domain.manager

import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.ui.feature.utils.BusStopDummy

interface MapsManager {

    fun getUserCurrentLocation(onResult: (Coordinate?) -> Unit)

    fun addGeofences(busStops: List<BusStopDummy>)

    fun removeGeofences(removeIds: List<Int>)

    fun getMapsApiKey(): String

    fun startLocationTrackingService()

    fun stopLocationTrackingService()

    fun calculateHaversine(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Float

    fun getLastKnownLocation(): Coordinate?
}