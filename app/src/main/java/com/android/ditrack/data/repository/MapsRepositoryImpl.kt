package com.android.ditrack.data.repository

import com.android.ditrack.data.mapper.toCoordinate
import com.android.ditrack.data.source.RemoteDataSource
import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.common.NetworkErrorType
import com.android.ditrack.domain.common.Result
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapsRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : MapsRepository {

    private val _isServiceRunning = MutableStateFlow(false)
    override val isServiceRunning = _isServiceRunning.asStateFlow()

    private val _event = MutableStateFlow<ApplicationModeState>(ApplicationModeState.Idle)
    override val event = _event.asStateFlow()

    private val _command = MutableStateFlow<ApplicationModeState>(ApplicationModeState.Idle)
    override val command = _command.asStateFlow()

    override fun setServiceRunning(isServiceRunning: Boolean) {
        _isServiceRunning.value = isServiceRunning
    }

    override suspend fun sendEventFromService(event: ApplicationModeState) {
        _event.value = event
    }

    override suspend fun sendCommandToService(command: ApplicationModeState) {
        _command.value = command
    }

    override suspend fun getRouteDirections(
        origin: String,
        destination: String,
        waypoints: String?,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType> {
        return remoteDataSource.getRouteDirections(
            origin = origin,
            destination = destination,
            waypoints = waypoints,
            apiKey = apiKey
        )
    }

    override fun getAllBusStops(): List<BusStopDummy> {
        return DataDummyProvider.getBusStops()
    }

    override fun getRoutePoints(): List<Coordinate> {
        return DataDummyProvider.getRoutePoints().map { it.toCoordinate() }
    }
}