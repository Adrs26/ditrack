package com.android.ditrack.ui.feature.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun String.showMessageWithToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}


fun LatLng.distanceTo(other: LatLng): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(other.latitude - this.latitude)
    val dLng = Math.toRadians(other.longitude - this.longitude)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(this.latitude)) *
            cos(Math.toRadians(other.latitude)) *
            sin(dLng / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}