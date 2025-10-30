package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.model.Coordinate

class GetCurrentLocationUseCase(
    private val mapsManager: MapsManager
) {
    operator fun invoke(onResult: (Coordinate?) -> Unit) {
        mapsManager.getUserCurrentLocation(onResult)
    }
}