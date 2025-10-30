package com.android.ditrack.ui.feature.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.common.GeofenceTransitionState
import com.android.ditrack.domain.common.NetworkErrorType
import com.android.ditrack.domain.common.onError
import com.android.ditrack.domain.common.onSuccess
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.domain.repository.UserSessionRepository
import com.android.ditrack.domain.usecase.GetBusStopsUseCase
import com.android.ditrack.domain.usecase.GetCurrentLocationUseCase
import com.android.ditrack.domain.usecase.StartDrivingModeUseCase
import com.android.ditrack.domain.usecase.StartWaitingModeUseCase
import com.android.ditrack.domain.usecase.StopWaitingModeUseCase
import com.android.ditrack.domain.usecase.SyncGeofenceUseCase
import com.android.ditrack.ui.common.UiState
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.DataDummyProvider
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
    private val mapsRepository: MapsRepository,
    private val syncGeofenceUseCase: SyncGeofenceUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getBusStopsUseCase: GetBusStopsUseCase,
    private val startWaitingModeUseCase: StartWaitingModeUseCase,
    private val startDrivingModeUseCase: StartDrivingModeUseCase,
    private val stopWaitingModeUseCase: StopWaitingModeUseCase
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
                        geofenceTransitionState = userSession[0] as GeofenceTransitionState,
                        busStopOriginName = busStopOriginName,
                    )
                }
            }
        }

        viewModelScope.launch {
            _mapsUiState.update { it.copy(busStops = getBusStopsUseCase()) }
        }

        viewModelScope.launch {
            userSessionRepository.getBusStopLocation().collect { location ->
                busStopOriginLocation = location.toLatLng()
            }
        }

        viewModelScope.launch {
            mapsRepository.event.collect { event ->
                when (event) {
                    ApplicationModeState.Idle -> Unit
                    ApplicationModeState.Wait -> Unit
                    ApplicationModeState.Drive -> startDrivingMode()
                    ApplicationModeState.Arrive -> stopWaitingMode(true)
                }
            }
        }
    }

    fun syncGeofence(isGranted: Boolean, isMapLoaded: Boolean) {
        if (isGranted && isMapLoaded) {
            viewModelScope.launch {
                syncGeofenceUseCase()
                delay(500)
                animateToUserLocation()
            }
        }
    }

    fun animateToUserLocation() {
        getCurrentLocationUseCase { latLng ->
            latLng?.let {
                viewModelScope.launch {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 16f)
                    _cameraUpdateEvent.send(cameraUpdate)
                }
            }
        }
    }

    fun startWaitingMode(
        destinationName: String,
        destinationLocation: LatLng
    ) {
        _mapsUiState.update { it.copy(routeInfo = UiState.Loading) }
        busStopDestinationLocation = destinationLocation

        viewModelScope.launch {
            val result = startWaitingModeUseCase(
                origin = busStopOriginLocation.toCoordinate(),
                destination = busStopDestinationLocation.toCoordinate(),
            )
            if (result != null) {
                result.onSuccess { (routeInfo, busStops) ->
                    val routeInfoState = RouteInfoState(
                        polylinePoints = routeInfo.polylinePoints.map { it.toLatLng() },
                        duration = routeInfo.duration,
                        distance = routeInfo.distance
                    )
                    _mapsUiState.update {
                        it.copy(
                            applicationModeState = ApplicationModeState.Wait,
                            busStops = busStops,
                            busStopDestinationName = destinationName,
                            routeInfo = UiState.Success(routeInfoState)
                        )
                    }
                    animateToUserLocation()
                }.onError { error ->
                    _mapsUiState.update { it.copy(routeInfo = UiState.Error(error)) }
                }
            } else {
                _mapsUiState.update {
                    it.copy(routeInfo = UiState.Error(NetworkErrorType.REQUEST_TIMEOUT))
                }
            }
        }
    }

    private fun startDrivingMode() {
        _mapsUiState.update { it.copy(routeInfo = UiState.Loading) }

        viewModelScope.launch {
            val result = startDrivingModeUseCase(
                origin = busStopOriginLocation.toCoordinate(),
                destination = busStopDestinationLocation.toCoordinate(),
            )
            result.onSuccess { (routeInfo, busStops) ->
                val routeInfoState = RouteInfoState(
                    polylinePoints = routeInfo.polylinePoints.map { it.toLatLng() },
                    duration = routeInfo.duration,
                    distance = routeInfo.distance
                )
                _mapsUiState.update {
                    it.copy(
                        applicationModeState = ApplicationModeState.Drive,
                        busStops = busStops,
                        routeInfo = UiState.Success(routeInfoState)
                    )
                }
                animateToUserLocation()
            }.onError { error ->
                _mapsUiState.update { it.copy(routeInfo = UiState.Error(error)) }
            }
        }
    }

    fun stopWaitingMode(isArriving : Boolean = false) {
        _mapsUiState.update { it.copy(routeInfo = UiState.Loading) }

        viewModelScope.launch {
            val busStops = stopWaitingModeUseCase()
            _mapsUiState.update {
                it.copy(
                    applicationModeState = if (isArriving) {
                        ApplicationModeState.Arrive
                    } else {
                        ApplicationModeState.Idle
                    },
                    routeInfo = UiState.Empty,
                    busStops = busStops
                )
            }
            animateToUserLocation()

            if (isArriving) {
                delay(3000)
                _mapsUiState.update { it.copy(applicationModeState = ApplicationModeState.Idle) }
            }
        }
    }

    private fun LatLng.toCoordinate() = Coordinate(latitude = latitude, longitude = longitude)
    private fun Coordinate.toLatLng() = LatLng(latitude, longitude)
}

data class MapsUiState(
    val applicationModeState: ApplicationModeState = ApplicationModeState.Idle,
    val geofenceTransitionState: GeofenceTransitionState = GeofenceTransitionState.Idle,
    val busStops: List<BusStopDummy> = emptyList(),
    val busStopOriginName: String = "",
    val busStopDestinationName: String = "",
    val routeInfo: UiState<RouteInfoState> = UiState.Empty
)

data class RouteInfoState(
    val polylinePoints: List<LatLng> = emptyList(),
    val duration: Int = 0,
    val distance: Double = 0.0
)