package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.ui.feature.utils.BusStopDummy

class StopWaitingModeUseCase(
    private val mapsManager: MapsManager,
    private val getBusStopsUseCase: GetBusStopsUseCase
) {
    operator fun invoke(): List<BusStopDummy> {
        mapsManager.stopLocationTrackingService()
        return getBusStopsUseCase()
    }
}