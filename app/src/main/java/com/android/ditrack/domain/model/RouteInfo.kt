package com.android.ditrack.domain.model

import com.google.android.gms.maps.model.LatLng

data class RouteInfo(
    val polylinePoints: List<LatLng>,
    val duration: Int,
    val distance: Double
)