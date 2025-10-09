package com.android.ditrack.domain.repository

import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result

interface MapsRepository {

    suspend fun getRouteDirections(
        origin: String,
        destination: String,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType>

    fun getAllBusStops(): List<BusStopDummy>
}