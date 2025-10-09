package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.google.android.gms.maps.model.LatLng

class GetBusStopsUseCase(
    private val mapsRepository: MapsRepository
) {
    operator fun invoke(filter: ((LatLng) -> Boolean)? = null): List<BusStopDummy> {
        val filteredBusStop = if (filter != null) {
            mapsRepository.getAllBusStops().filter { busStop ->
                filter(busStop.latLng)
            }
        } else {
            mapsRepository.getAllBusStops()
        }

        return filteredBusStop
    }
}