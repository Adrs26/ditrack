package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy

class GetBusStopsUseCase(
    private val mapsRepository: MapsRepository
) {
    operator fun invoke(filter: ((Coordinate) -> Boolean)? = null): List<BusStopDummy> {
        val filteredBusStop = if (filter != null) {
            mapsRepository.getAllBusStops().filter { busStop ->
                filter(
                    Coordinate(
                        latitude = busStop.latLng.latitude,
                        longitude = busStop.latLng.longitude
                    )
                )
            }
        } else {
            mapsRepository.getAllBusStops()
        }

        return filteredBusStop
    }
}