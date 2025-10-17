package com.android.ditrack.domain.usecase

import com.android.ditrack.data.manager.MapsManager
import com.android.ditrack.domain.model.ApplicationMode
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result
import com.android.ditrack.ui.feature.utils.distanceTo
import com.google.android.gms.maps.model.LatLng

class StartDrivingModeUseCase(
    private val mapsManager: MapsManager,
    private val mapsRepository: MapsRepository,
    private val getRouteInfoUseCase: GetRouteInfoUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase
) {
    suspend operator fun invoke(
        origin: LatLng,
        destination: LatLng,
    ): Result<Pair<RouteInfo, List<BusStopDummy>>, NetworkErrorType> {
        mapsRepository.sendCommandToService(ApplicationMode.DRIVING)

        val routeInfo = getRouteInfoUseCase(
            destination = destination,
            apiKey = mapsManager.getMapsApiKey()
        )
        val busStops = getBusStopsUseCase { location ->
            (location.distanceTo(origin) < 1.0) || (location.distanceTo(destination) < 1.0)
        }

        return if (routeInfo is Result.Success) {
            Result.Success(Pair(routeInfo.data, busStops))
        } else {
            Result.Error((routeInfo as Result.Error).error)
        }
    }
}