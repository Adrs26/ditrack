package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.common.GeofenceTransitionState
import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SyncGeofenceUseCase(
    private val mapsManager: MapsManager,
    private val userSessionRepository: UserSessionRepository,
    private val mapsRepository: MapsRepository
) {
    suspend operator fun invoke() {
        val savedBusStop = userSessionRepository.getBusStopId().first()
        val localBusStops = userSessionRepository.getBusStopIds().first()
        val remoteBusStops = mapsRepository.getAllBusStops().map { it.id }

        if (localBusStops != remoteBusStops) {
            val toRemove = localBusStops - remoteBusStops
            val toAdd = remoteBusStops - localBusStops

            mapsManager.removeGeofences(toRemove)

            if (toAdd.isNotEmpty()) {
                val busStopsToAdd = mapsRepository.getAllBusStops().filter { it.id in toAdd }
                mapsManager.addGeofences(busStopsToAdd)
            }

            userSessionRepository.setBusStopIds(remoteBusStops)

            if (savedBusStop in toRemove) {
                userSessionRepository.setGeofenceTransition(GeofenceTransitionState.Idle)
                userSessionRepository.setBusStopId(-1)
                userSessionRepository.setBusStopLocation(Coordinate(0.0, 0.0))
            }
        }

        val (isInside, busStop) = getCurrentGeofenceStatus(mapsRepository.getAllBusStops())
        if (isInside) {
            userSessionRepository.setGeofenceTransition(GeofenceTransitionState.Enter)
            userSessionRepository.setBusStopId(busStop.id)
            userSessionRepository.setBusStopLocation(
                Coordinate(busStop.latLng.latitude, busStop.latLng.longitude)
            )
        } else {
            userSessionRepository.setGeofenceTransition(GeofenceTransitionState.Exit)
            userSessionRepository.setBusStopId(busStop.id)
            userSessionRepository.setBusStopLocation(
                Coordinate(busStop.latLng.latitude, busStop.latLng.longitude)
            )
        }
    }

    private suspend fun getCurrentGeofenceStatus(
        busStops: List<BusStopDummy>
    ): Pair<Boolean, BusStopDummy> = withContext(Dispatchers.IO) {
        try {
            val location = mapsManager.getLastKnownLocation()
                ?: return@withContext Pair(false, BusStopDummy())

            val userLatLng = Coordinate(location.latitude, location.longitude)
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
        userLatLng: Coordinate,
        busStop: BusStopDummy
    ): Boolean {
        val distance = mapsManager.calculateHaversine(
            startCoordinate = userLatLng,
            endCoordinate = Coordinate(busStop.latLng.latitude, busStop.latLng.longitude)
        )
        return distance <= 100f
    }
}