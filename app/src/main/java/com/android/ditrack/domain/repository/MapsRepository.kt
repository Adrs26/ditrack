package com.android.ditrack.domain.repository

import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.common.NetworkError
import com.android.ditrack.domain.common.Result
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.ui.feature.utils.BusStopDummy
import kotlinx.coroutines.flow.StateFlow

interface MapsRepository {

    val isServiceRunning: StateFlow<Boolean>
    val event: StateFlow<ApplicationModeState>
    val command: StateFlow<ApplicationModeState>

    fun setServiceRunning(isServiceRunning: Boolean)

    suspend fun sendEventFromService(event: ApplicationModeState)

    suspend fun sendCommandToService(command: ApplicationModeState)

    suspend fun getRouteDirections(
        origin: String,
        destination: String,
        waypoints: String?,
        apiKey: String
    ): Result<RouteInfo, NetworkError>

    fun getAllBusStops(): List<BusStopDummy>

    fun getRoutePoints(): List<Coordinate>
}