package com.android.ditrack.ui.feature.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.ditrack.receiver.GeofenceBroadcastReceiver
import com.android.ditrack.service.LocationTrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

class MapsManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geofencingClient: GeofencingClient
) {
    fun getUserCurrentLocation(onResult: (LatLng?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(LatLng(location.latitude, location.longitude))
            } else {
                onResult(null)
            }
        }
    }

    fun addGeofences(busStops: List<BusStopsDummy>) {
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
                    .setRequestId(busStop.id.toString())
                    .setCircularRegion(busStop.latLng.latitude, busStop.latLng.longitude, 100f)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
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

    fun removeGeofences(removeIds: List<Int>) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (removeIds.isEmpty()) {
            return
        } else {
            geofencingClient.removeGeofences(removeIds.map { it.toString() })
        }
    }

    fun startLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        context.stopService(serviceIntent)
    }
}