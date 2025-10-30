package com.android.ditrack.data.mapper

import android.location.Location
import com.android.ditrack.domain.model.Coordinate
import com.google.android.gms.maps.model.LatLng

fun LatLng.toCoordinate() = Coordinate(
    latitude = latitude,
    longitude = longitude
)

fun Location.toCoordinate() = Coordinate(
    latitude = latitude,
    longitude = longitude
)

fun Coordinate.toLatLng() = LatLng(latitude, longitude)