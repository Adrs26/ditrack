package com.android.ditrack.domain.usecase

import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.ui.feature.utils.MapsManager

class SetApplicationModeUseCase(
    private val userSessionRepository: UserSessionRepository,
    private val mapsManager: MapsManager
) {
    suspend operator fun invoke(applicationMode: ApplicationMode) {
        if (applicationMode == ApplicationMode.DEFAULT) {
            mapsManager.stopLocationTrackingService()
        } else if (applicationMode == ApplicationMode.WAITING) {
            mapsManager.startLocationTrackingService()
        }

        userSessionRepository.setApplicationMode(applicationMode)
    }
}