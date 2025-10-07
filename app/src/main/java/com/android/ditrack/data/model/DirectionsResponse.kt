package com.android.ditrack.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DirectionsResponse(
    val routes: List<Route>
)

@Serializable
data class Route(
    @SerialName("overview_polyline")
    val overviewPolyline: OverviewPolyline,
    val legs: List<Leg>
)

@Serializable
data class OverviewPolyline(
    val points: String
)

@Serializable
data class Leg(
    val duration: Duration,
    val distance: Distance
)

@Serializable
data class Duration(
    val text: String
)

@Serializable
data class Distance(
    val text: String
)