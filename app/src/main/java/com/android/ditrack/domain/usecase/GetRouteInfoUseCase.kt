package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result
import com.google.android.gms.maps.model.LatLng

class GetRouteInfoUseCase(
    private val mapsRepository: MapsRepository
) {
    suspend operator fun invoke(
        destination: LatLng,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType> {
        val origin = LatLng(-7.0550504, 110.4428640)
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"

        val routeInfo = mapsRepository.getRouteDirections(
            origin = originStr,
            destination = destinationStr,
            apiKey = apiKey
        )

        return routeInfo
    }
}