package com.android.ditrack.ui.feature.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.domain.usecase.GetBusStopsUseCase
import com.android.ditrack.domain.usecase.GetRouteInfoUseCase
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapsViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val syncGeofenceUseCase: SyncGeofenceUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase,
    private val getRouteInfoUseCase: GetRouteInfoUseCase,
    private val mapsManager: MapsManager
) : ViewModel() {

    private val _cameraUpdateEvent = Channel<CameraUpdate>(Channel.BUFFERED)
    val cameraUpdateEvent = _cameraUpdateEvent.receiveAsFlow()

    private val _mapsUiState = MutableStateFlow(MapsUiState())
    val mapsUiState = _mapsUiState.asStateFlow()

    private var busStopOriginLocation = LatLng(0.0, 0.0)
    private var busStopDestinationLocation = LatLng(0.0, 0.0)

    init {
        viewModelScope.launch {
            combine(
                userSessionRepository.getGeofenceTransition(),
                userSessionRepository.getBusStopId()
            ) { transition, id ->
                listOf(transition, id)
            }.collect { userSession ->
                val busStopOriginName = DataDummyProvider.getBusStops()
                    .firstOrNull { it.id == (userSession[1] as Int) }?.name ?: ""

                _mapsUiState.update {
                    it.copy(
                        geofenceTransition = userSession[0] as GeofenceTransition,
                        busStopOriginName = busStopOriginName,
                    )
                }
            }
        }

        viewModelScope.launch {
            _mapsUiState.update {
                it.copy(busStops = getBusStopsUseCase())
            }
        }

        viewModelScope.launch {
            userSessionRepository.getBusStopLocation().collect { location ->
                busStopOriginLocation = location
            }
        }
    }

    fun syncGeofence(isGranted: Boolean, isMapLoaded: Boolean) {
        if (isGranted && isMapLoaded) {
            viewModelScope.launch {
                syncGeofenceUseCase()
                delay(500)
                animateToUserLocation()
                Log.d("MapsViewModel", "Geofence synced")
            }
        }
    }

    fun animateToUserLocation() {
        mapsManager.getUserCurrentLocation { latLng ->
            latLng?.let {
                viewModelScope.launch {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 16f)
                    _cameraUpdateEvent.send(cameraUpdate)
                }
            }
        }
    }

    fun startWaitingMode(destinationName: String, destinationLocation: LatLng, apiKey: String) {
        _mapsUiState.update {
            it.copy(
                applicationMode = ApplicationMode.WAITING,
                busStopDestinationName = destinationName
            )
        }
        busStopDestinationLocation = destinationLocation

        viewModelScope.launch {
            getRouteInfo(busStopOriginLocation, apiKey)
            delay(500)
            if (_mapsUiState.value.routeInfo is UiState.Success) {
                mapsManager.startLocationTrackingService()
                animateToUserLocation()
            }
        }
    }

    fun stopWaitingMode() {
        viewModelScope.launch {
            _mapsUiState.update {
                it.copy(
                    applicationMode = ApplicationMode.IDLING,
                    routeInfo = UiState.Empty,
                    busStops = getBusStopsUseCase()
                )
            }
            mapsManager.stopLocationTrackingService()
            animateToUserLocation()
        }
    }

    fun startDrivingMode(apiKey: String) {
        _mapsUiState.update { it.copy(applicationMode = ApplicationMode.DRIVING) }

        viewModelScope.launch {
            getRouteInfo(busStopDestinationLocation, apiKey)
            delay(500)
            if (_mapsUiState.value.routeInfo is UiState.Success) {
                mapsManager.stopLocationTrackingService()
                delay(1000)
                mapsManager.startLocationTrackingService()
                animateToUserLocation()
            }
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
                                val origin = busStopOriginLocation
                                val destination = busStopDestinationLocation
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
    val applicationMode: ApplicationMode = ApplicationMode.IDLING,
    val geofenceTransition: GeofenceTransition = GeofenceTransition.DEFAULT,
    val busStops: List<BusStopDummy> = emptyList(),
    val busStopOriginName: String = "",
    val busStopDestinationName: String = "",
    val routeInfo: UiState<RouteInfoState> = UiState.Empty
)

enum class ApplicationMode {
    IDLING, WAITING, DRIVING, ARRIVING
}


data class RouteInfoState(
    val polylinePoints: List<LatLng> = emptyList(),
    val duration: String = "",
    val distance: String = ""
)