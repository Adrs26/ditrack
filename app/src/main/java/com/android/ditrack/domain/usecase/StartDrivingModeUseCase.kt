package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.common.NetworkError
import com.android.ditrack.domain.common.Result
import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository

class StartDrivingModeUseCase(
    private val mapsManager: MapsManager,
    private val mapsRepository: MapsRepository,
    private val getRouteInfoUseCase: GetRouteInfoUseCase
) {
    suspend operator fun invoke(
        origin: Coordinate,
        destination: Coordinate,
    ): Result<RouteInfo, NetworkError> {
        val routeInfo = getRouteInfoUseCase(
            destination = destination,
            apiKey = mapsManager.getMapsApiKey()
        )

        return if (routeInfo is Result.Success) {
            mapsRepository.sendCommandToService(ApplicationModeState.Drive)
            Result.Success(routeInfo.data)
        } else {
            Result.Error((routeInfo as Result.Error).error)
        }
    }
}