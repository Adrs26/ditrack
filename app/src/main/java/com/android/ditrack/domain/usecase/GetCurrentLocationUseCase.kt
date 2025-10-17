package com.android.ditrack.domain.usecase

import com.android.ditrack.data.manager.MapsManager
import com.google.android.gms.maps.model.LatLng

class GetCurrentLocationUseCase(
    private val mapsManager: MapsManager
) {
    operator fun invoke(onResult: (LatLng?) -> Unit) {
        mapsManager.getUserCurrentLocation(onResult)
    }
}