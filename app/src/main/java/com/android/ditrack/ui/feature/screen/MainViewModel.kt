package com.android.ditrack.ui.feature.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.domain.usecase.SetApplicationModeUseCase
import com.android.ditrack.domain.usecase.SyncGeofenceUseCase
import com.android.ditrack.ui.feature.utils.MapsManager
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    userSessionRepository: UserSessionRepository,
    private val syncGeofenceUseCase: SyncGeofenceUseCase,
    private val setApplicationModeUseCase: SetApplicationModeUseCase,
    private val mapsManager: MapsManager
) : ViewModel() {

    private val _cameraUpdateEvent = MutableSharedFlow<CameraUpdate>()
    val cameraUpdateEvent: SharedFlow<CameraUpdate> = _cameraUpdateEvent.asSharedFlow()

    val uiState = combine(
        userSessionRepository.getApplicationMode(),
        userSessionRepository.getGeofenceTransition(),
        userSessionRepository.getBusStopId()
    ) { mode, transition, id ->
        MainUiState(mode, transition, id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    fun syncGeofence(isGranted: Boolean, isMapLoaded: Boolean) {
        if (isGranted && isMapLoaded) {
            animateToUserLocation()
            viewModelScope.launch { syncGeofenceUseCase() }
        }
    }

    fun setApplicationMode(applicationMode: ApplicationMode) {
        if (applicationMode == ApplicationMode.WAITING) animateToUserLocation()
        viewModelScope.launch { setApplicationModeUseCase(applicationMode) }
    }

    fun animateToUserLocation() {
        mapsManager.getUserCurrentLocation { latLng ->
            latLng?.let {
                viewModelScope.launch {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 16f)
                    _cameraUpdateEvent.emit(cameraUpdate)
                }
            }
        }
    }
}

data class MainUiState(
    val applicationMode: ApplicationMode = ApplicationMode.DEFAULT,
    val geofenceTransition: GeofenceTransition = GeofenceTransition.DEFAULT,
    val busStopId: Int = -1
)