package com.android.ditrack.domain.model

data class RouteInfo(
    val polylinePoints: List<Coordinate>,
    val duration: Int,
    val distance: Double
)