package com.android.ditrack.ui.feature.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.android.ditrack.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

object MapsUtil {
    fun getCurrentLatLng(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onResult: (LatLng?) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(LatLng(location.latitude, location.longitude))
                } else {
                    onResult(null)
                }
            }
    }

    fun addGeofences(
        context: Context,
        busStops: List<LatLng>,
        geofencingClient: GeofencingClient
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val geofences = mutableListOf<Geofence>()
        busStops.forEachIndexed { index, busStop ->
            geofences.add(
                Geofence.Builder()
                    .setRequestId((index + 1).toString())
                    .setCircularRegion(busStop.latitude, busStop.longitude, 100f)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            )
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
    }
}