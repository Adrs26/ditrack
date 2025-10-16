package com.android.ditrack.domain.repository

import com.android.ditrack.domain.model.ApplicationMode
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result
import kotlinx.coroutines.flow.StateFlow

interface MapsRepository {

    val isServiceRunning: StateFlow<Boolean>
    val event: StateFlow<ApplicationMode>
    val command: StateFlow<ApplicationMode>

    fun setServiceRunning(isServiceRunning: Boolean)

    suspend fun sendEventFromService(event: ApplicationMode)

    suspend fun sendCommandToService(command: ApplicationMode)

    suspend fun getRouteDirections(
        origin: String,
        destination: String,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType>

    fun getAllBusStops(): List<BusStopDummy>
}