package com.android.ditrack.domain.usecase

import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.MapsManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SyncGeofenceUseCase(
    private val userSessionRepository: UserSessionRepository,
    private val mapsManager: MapsManager
) {
    suspend operator fun invoke() {
        val savedBusStop = userSessionRepository.getBusStopId().first()
        val localBusStops = userSessionRepository.getBusStopIds().first()
        val remoteBusStops = DataDummyProvider.getBusStops().map { it.id }

        if (localBusStops != remoteBusStops) {
            val toRemove = localBusStops - remoteBusStops
            val toAdd = remoteBusStops - localBusStops

            mapsManager.removeGeofences(toRemove)

            if (toAdd.isNotEmpty()) {
                val busStopsToAdd = DataDummyProvider.getBusStops().filter { it.id in toAdd }
                mapsManager.addGeofences(busStopsToAdd)
            }

            userSessionRepository.setBusStopIds(remoteBusStops)

            if (savedBusStop in toRemove) {
                userSessionRepository.setGeofenceTransition(GeofenceTransition.DEFAULT)
                userSessionRepository.setBusStopId(-1)
                userSessionRepository.setBusStopLocation(LatLng(0.0, 0.0))
            }
        }

        val (isInside, busStop) = getCurrentGeofenceStatus(DataDummyProvider.getBusStops())
        if (isInside) {
            userSessionRepository.setGeofenceTransition(GeofenceTransition.ENTER)
            userSessionRepository.setBusStopId(busStop.id)
            userSessionRepository.setBusStopLocation(busStop.latLng)
        } else {
            userSessionRepository.setGeofenceTransition(GeofenceTransition.EXIT)
            userSessionRepository.setBusStopId(busStop.id)
            userSessionRepository.setBusStopLocation(busStop.latLng)
        }
    }

    private suspend fun getCurrentGeofenceStatus(
        busStops: List<BusStopDummy>
    ): Pair<Boolean, BusStopDummy> = withContext(Dispatchers.IO) {
        try {
            val location = mapsManager.getLastKnownLocation()
                ?: return@withContext Pair(false, BusStopDummy())

            val userLatLng = LatLng(location.latitude, location.longitude)
            val busStop = busStops.firstOrNull { isInsideGeofence(userLatLng, it) }

            if (busStop != null) {
                Pair(true, busStop)
            } else {
                Pair(false, BusStopDummy())
            }
        } catch (_: Exception) {
            Pair(false, BusStopDummy())
        }
    }

    private fun isInsideGeofence(
        userLatLng: LatLng,
        busStop: BusStopDummy
    ): Boolean {
        val distance = mapsManager.calculateHaversine(
            userLat = userLatLng.latitude,
            userLng = userLatLng.longitude,
            busStopLat = busStop.latLng.latitude,
            busStopLng = busStop.latLng.longitude
        )
        return distance <= 100f
    }
}