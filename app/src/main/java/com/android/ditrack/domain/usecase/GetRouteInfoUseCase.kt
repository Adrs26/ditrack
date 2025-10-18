package com.android.ditrack.domain.usecase

import com.android.ditrack.domain.model.RouteInfo
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.NetworkErrorType
import com.android.ditrack.ui.feature.utils.Result
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GetRouteInfoUseCase(
    private val mapsRepository: MapsRepository
) {
    suspend operator fun invoke(
        destination: LatLng,
        apiKey: String
    ): Result<RouteInfo, NetworkErrorType> {
        val origin = LatLng(-7.0550504, 110.4428640)
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"
        val sampledPoints = getWaypoints(
            routePoints = mapsRepository.getRoutePoints(),
            currentBusLocation = origin,
            destinationLocation = destination
        )

        val waypoints = if (sampledPoints.size > 2) {
            convertToWaypointsParam(sampledPoints.subList(1, sampledPoints.size - 1))
        } else {
            null
        }

        val routeInfo = mapsRepository.getRouteDirections(
            origin = originStr,
            destination = destinationStr,
            waypoints = waypoints,
            apiKey = apiKey
        )

        return routeInfo
    }

    private fun getWaypoints(
        routePoints: List<LatLng>,
        currentBusLocation: LatLng,
        destinationLocation: LatLng,
        maxPoints: Int = 12
    ): List<LatLng> {
        val busIndex = findClosestIndex(routePoints, currentBusLocation)
        val destIndex = findClosestIndex(routePoints, destinationLocation)

        val totalIndices = mutableListOf<Int>()
        var idx = busIndex
        do {
            totalIndices.add(idx)
            idx = (idx + 1) % routePoints.size
        } while (idx != (destIndex + 1) % routePoints.size)

        if (totalIndices.size <= maxPoints) {
            return totalIndices.map { routePoints[it] }
        }

        val step = totalIndices.size.toDouble() / maxPoints
        val selectedIndices = (0 until maxPoints).map { i ->
            totalIndices[(i * step).toInt()]
        }

        return selectedIndices.map { routePoints[it] }
    }

    fun findClosestIndex(list: List<LatLng>, target: LatLng): Int {
        return list.indices.minByOrNull { index ->
            calculateDistance(
                lat1 = list[index].latitude,
                lon1 = list[index].longitude,
                lat2 = target.latitude,
                lon2 = target.longitude
            )
        } ?: 0
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun convertToWaypointsParam(points: List<LatLng>): String {
        return points.joinToString("|") { "${it.latitude},${it.longitude}" }
    }
}