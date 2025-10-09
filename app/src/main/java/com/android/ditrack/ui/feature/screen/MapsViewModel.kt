package com.android.ditrack.ui.feature.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.domain.usecase.GetBusStopsUseCase
import com.android.ditrack.domain.usecase.GetRouteInfoUseCase
import com.android.ditrack.domain.usecase.SetApplicationModeUseCase
import com.android.ditrack.domain.usecase.SyncGeofenceUseCase
import com.android.ditrack.ui.common.UiState
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.MapsManager
import com.android.ditrack.ui.feature.utils.distanceTo
import com.android.ditrack.ui.feature.utils.onError
import com.android.ditrack.ui.feature.utils.onSuccess
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapsViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val syncGeofenceUseCase: SyncGeofenceUseCase,
    private val setApplicationModeUseCase: SetApplicationModeUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase,
    private val getRouteInfoUseCase: GetRouteInfoUseCase,
    private val mapsManager: MapsManager
) : ViewModel() {

    private val _cameraUpdateEvent = MutableSharedFlow<CameraUpdate>()
    val cameraUpdateEvent: SharedFlow<CameraUpdate> = _cameraUpdateEvent.asSharedFlow()

    private val _mapsUiState = MutableStateFlow(MapsUiState())
    val mapsUiState = _mapsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userSessionRepository.getApplicationMode(),
                userSessionRepository.getGeofenceTransition(),
                userSessionRepository.getBusStopId(),
                userSessionRepository.getBusStopLocation(),
            ) { mode, transition, id, location ->
                listOf(mode, transition, id, location)
            }.collect { userSession ->
                val busStopOriginName = DataDummyProvider.getBusStops()
                    .firstOrNull { it.id == (userSession[2] as Int) }?.name ?: ""

                _mapsUiState.update {
                    it.copy(
                        applicationMode = userSession[0] as ApplicationMode,
                        geofenceTransition = userSession[1] as GeofenceTransition,
                        busStopOriginName = busStopOriginName,
                        busStopOriginLocation = userSession[3] as LatLng
                    )
                }
            }
        }

        viewModelScope.launch {
            _mapsUiState.update {
                it.copy(busStops = getBusStopsUseCase())
            }
        }
    }

    fun syncGeofence(isGranted: Boolean, isMapLoaded: Boolean) {
        if (isGranted && isMapLoaded) {
            animateToUserLocation()
            viewModelScope.launch { syncGeofenceUseCase() }
        }
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

    fun startWaitingMode(destinationName: String, destinationLocation: LatLng, apiKey: String) {
        viewModelScope.launch {
            val routeDestination = _mapsUiState.value.busStopOriginLocation
            setBusStopDestination(destinationName, destinationLocation)
            getRouteInfo(routeDestination, apiKey)

            delay(1000)
            if (_mapsUiState.value.routeInfo is UiState.Success) {
                setApplicationModeUseCase(ApplicationMode.WAITING)
                animateToUserLocation()
            }
        }
    }

    fun stopWaitingMode() {
        viewModelScope.launch {
            setApplicationModeUseCase(ApplicationMode.DEFAULT)
            _mapsUiState.update {
                it.copy(
                    routeInfo = UiState.Empty,
                    busStops = getBusStopsUseCase()
                )
            }
            animateToUserLocation()
        }
    }

    fun startDrivingMode(apiKey: String) {
        viewModelScope.launch {
            val routeDestination = _mapsUiState.value.busStopDestinationLocation
            getRouteInfo(routeDestination, apiKey)

            delay(1000)
            if (_mapsUiState.value.routeInfo is UiState.Success) {
                setApplicationModeUseCase(ApplicationMode.DRIVING)
                animateToUserLocation()
            }
        }
    }

    private fun setBusStopDestination(name: String, location: LatLng) {
        _mapsUiState.update {
            it.copy(
                busStopDestinationName = name,
                busStopDestinationLocation = location
            )
        }
    }

    private fun getRouteInfo(destination: LatLng, apiKey: String) {
        _mapsUiState.update {
            it.copy(routeInfo = UiState.Loading)
        }

        viewModelScope.launch {
            getRouteInfoUseCase(destination, apiKey)
                .onSuccess { data ->
                    val routeInfoState = RouteInfoState(
                        polylinePoints = data.polylinePoints,
                        duration = data.duration,
                        distance = data.distance
                    )
                    _mapsUiState.update {
                        it.copy(
                            routeInfo = UiState.Success(routeInfoState),
                            busStops = getBusStopsUseCase { location ->
                                val origin = _mapsUiState.value.busStopOriginLocation
                                val destination = _mapsUiState.value.busStopDestinationLocation
                                (location.distanceTo(origin) < 1.0) || (location.distanceTo(destination) < 1.0)
                            }
                        )
                    }
                }
                .onError { error ->
                    _mapsUiState.update { it.copy(routeInfo = UiState.Error(error)) }
                }
        }
    }
}

data class MapsUiState(
    val applicationMode: ApplicationMode = ApplicationMode.DEFAULT,
    val geofenceTransition: GeofenceTransition = GeofenceTransition.DEFAULT,
    val busStops: List<BusStopDummy> = emptyList(),
    val busStopOriginName: String = "",
    val busStopOriginLocation: LatLng = LatLng(0.0, 0.0),
    val busStopDestinationName: String = "",
    val busStopDestinationLocation: LatLng = LatLng(0.0, 0.0),
    val routeInfo: UiState<RouteInfoState> = UiState.Empty
)

data class RouteInfoState(
    val polylinePoints: List<LatLng> = emptyList(),
    val duration: String = "",
    val distance: String = ""
)