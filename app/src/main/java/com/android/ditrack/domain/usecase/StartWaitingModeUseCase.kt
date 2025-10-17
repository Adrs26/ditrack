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
import kotlinx.coroutines.delay

class StartWaitingModeUseCase(
    private val mapsManager: MapsManager,
    private val mapsRepository: MapsRepository,
    private val getRouteInfoUseCase: GetRouteInfoUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase
) {
    suspend operator fun invoke(
        origin: LatLng,
        destination: LatLng,
    ): Result<Pair<RouteInfo, List<BusStopDummy>>, NetworkErrorType>? {
        if (mapsRepository.isServiceRunning.value) {
            mapsRepository.sendCommandToService(ApplicationMode.IDLING)
            delay(500)
            mapsRepository.sendCommandToService(ApplicationMode.WAITING)
        } else {
            mapsManager.startLocationTrackingService()
        }

        delay(1000)
        return if (mapsRepository.isServiceRunning.value) {
            mapsRepository.sendCommandToService(ApplicationMode.WAITING)

            val routeInfo = getRouteInfoUseCase(
                destination = origin,
                apiKey = mapsManager.getMapsApiKey()
            )
            val busStops = getBusStopsUseCase { location ->
                (location.distanceTo(origin) < 1.0) || (location.distanceTo(destination) < 1.0)
            }

            if (routeInfo is Result.Success) {
                Result.Success(Pair(routeInfo.data, busStops))
            } else {
                Result.Error((routeInfo as Result.Error).error)
            }
        } else { null }
    }
}