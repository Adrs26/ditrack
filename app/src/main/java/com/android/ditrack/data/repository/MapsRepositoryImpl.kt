package com.android.ditrack.data.repository

import android.util.Log
import com.android.ditrack.data.model.DirectionsResponse
import com.android.ditrack.domain.model.ApplicationMode
import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.MapsManager
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException

class MapsRepositoryImpl(
    private val client: HttpClient,
    private val mapsManager: MapsManager
) : MapsRepository {

    private val _isServiceRunning = MutableStateFlow(false)
    override val isServiceRunning = _isServiceRunning.asStateFlow()

    private val _event = MutableStateFlow<ApplicationMode>(ApplicationMode.IDLING)
    override val event = _event.asStateFlow()

    private val _command = MutableStateFlow<ApplicationMode>(ApplicationMode.IDLING)
    override val command = _command.asStateFlow()

    override fun setServiceRunning(isServiceRunning: Boolean) {
        _isServiceRunning.value = isServiceRunning
    }

    override suspend fun sendEventFromService(event: ApplicationMode) {
        _event.value = event
    }

    override suspend fun sendCommandToService(command: ApplicationMode) {
        _command.value = command
    }

    override suspend fun getRouteDirections(
        origin: String,
        destination: String,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType> {
        return try {
            val response = client.get("https://maps.googleapis.com/maps/api/directions/json") {
                url {
                    parameters.append("origin", origin)
                    parameters.append("destination", destination)
                    parameters.append("key", apiKey)
                }
            }

            when (response.status.value) {
                in 200..299 -> {
                    val directionsResponse = response.body<DirectionsResponse>()
                    Log.d("MainRepositoryImpl", "getRouteDirections: $directionsResponse")
                    val routeInfo = directionsResponse.toDomain()

                    if (routeInfo != null) {
                        Result.Success(routeInfo)
                    } else {
                        Result.Error(NetworkErrorType.NO_ROUTE_FOUND)
                    }
                }
                401 -> Result.Error(NetworkErrorType.UNAUTHORIZED)
                408 -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                409 -> Result.Error(NetworkErrorType.CONFLICT)
                413 -> Result.Error(NetworkErrorType.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkErrorType.SERVER_ERROR)
                else -> Result.Error(NetworkErrorType.UNKNOWN)
            }
        } catch (e: Exception) {
            when (e) {
                is UnresolvedAddressException -> Result.Error(NetworkErrorType.NO_INTERNET)
                is SerializationException -> Result.Error(NetworkErrorType.SERIALIZATION)
                is SocketTimeoutException -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                else -> Result.Error(NetworkErrorType.UNKNOWN)
            }
        }
    }

    override fun getAllBusStops(): List<BusStopDummy> {
        return DataDummyProvider.getBusStops()
    }

    private fun DirectionsResponse.toDomain(): RouteInfo? {
        val route = this.routes.firstOrNull() ?: return null

        val points = mapsManager.decodePolyLine(route.overviewPolyline.points)
        val duration = route.legs.firstOrNull()?.duration?.text ?: ""
        val distance = route.legs.firstOrNull()?.distance?.text ?: ""

        return RouteInfo(
            polylinePoints = points,
            duration = duration,
            distance = distance
        )
    }
}