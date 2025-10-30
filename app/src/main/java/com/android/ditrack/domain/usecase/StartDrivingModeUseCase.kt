package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.common.NetworkErrorType
import com.android.ditrack.domain.common.Result
import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class StartDrivingModeUseCase(
    private val mapsManager: MapsManager,
    private val mapsRepository: MapsRepository,
    private val getRouteInfoUseCase: GetRouteInfoUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase
) {
    suspend operator fun invoke(
        origin: Coordinate,
        destination: Coordinate,
    ): Result<Pair<RouteInfo, List<BusStopDummy>>, NetworkErrorType> {
        mapsRepository.sendCommandToService(ApplicationModeState.Drive)

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

    fun Coordinate.distanceTo(other: Coordinate): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(other.latitude - this.latitude)
        val dLng = Math.toRadians(other.longitude - this.longitude)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(this.latitude)) *
                cos(Math.toRadians(other.latitude)) *
                sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}