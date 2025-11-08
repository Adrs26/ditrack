package com.android.ditrack.data.source

import com.android.ditrack.data.mapper.toCoordinate
import com.android.ditrack.data.model.DirectionsResponse
import com.android.ditrack.domain.common.NetworkErrorType
import com.android.ditrack.domain.common.Result
import com.android.ditrack.domain.model.RouteInfo
import com.google.maps.android.ktx.utils.toLatLngList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException
import kotlin.math.round

class RemoteDataSource(private val client: HttpClient) {

    suspend fun getRouteDirections(
        origin: String,
        destination: String,
        waypoints: String?,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType> {
        return try {
            val response = client.get("https://maps.googleapis.com/maps/api/directions/json") {
                url {
                    parameters.append("origin", origin)
                    parameters.append("destination", destination)
                    if (waypoints != null) parameters.append("waypoints", waypoints)
                    parameters.append("key", apiKey)
                }
            }

            when (response.status.value) {
                in 200..299 -> {
                    val directionsResponse = response.body<DirectionsResponse>()
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
                is SerializationException -> Result.Error(NetworkErrorType.SERIALIZATION)
                is SocketTimeoutException -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                else -> Result.Error(NetworkErrorType.NO_INTERNET)
            }
        }
    }

    private fun DirectionsResponse.toDomain(): RouteInfo? {
        val route = this.routes.firstOrNull() ?: return null

        val points = route.overviewPolyline.points.toLatLngList()
        var totalDurationSeconds = 0
        var totalDistanceMeters = 0

        for (leg in route.legs) {
            totalDurationSeconds += leg.duration.value
            totalDistanceMeters += leg.distance.value
        }

        return RouteInfo(
            polylinePoints = points.map { it.toCoordinate() },
            duration = round(totalDurationSeconds / 60.0).toInt(),
            distance = totalDistanceMeters / 1000.0
        )
    }
}