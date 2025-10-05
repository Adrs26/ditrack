package com.android.ditrack.domain.usecase

import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.MapsManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first

class SyncGeofenceUseCase(
    private val userSessionRepository: UserSessionRepository,
    private val mapsManager: MapsManager
) {
    suspend operator fun invoke() {
        val savedBusStop = userSessionRepository.getBusStopId().first()
        val localBusStops = userSessionRepository.getBusStopIds().first()
        // Dummy data for api call
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
    }
}