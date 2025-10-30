package com.android.ditrack.data.manager

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.ditrack.R
import com.android.ditrack.data.mapper.toCoordinate
import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.receiver.GeofenceBroadcastReceiver
import com.android.ditrack.service.LocationTrackingService
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class MapsManagerImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geofencingClient: GeofencingClient,
) : MapsManager {
    override fun getUserCurrentLocation(onResult: (Coordinate?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(LatLng(location.latitude, location.longitude).toCoordinate())
            } else {
                onResult(null)
            }
        }
    }

    override fun addGeofences(busStops: List<BusStopDummy>) {
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
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
                    )
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

    override fun removeGeofences(removeIds: List<Int>) {
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

    override fun getMapsApiKey() = context.getString(R.string.maps_api_key)

    override fun startLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    override fun stopLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        context.stopService(serviceIntent)
    }

    override fun calculateHaversine(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            startCoordinate.latitude,
            startCoordinate.longitude,
            endCoordinate.latitude,
            endCoordinate.longitude,
            results
        )
        return results[0]
    }

    override fun getLastKnownLocation(): Coordinate? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val task: Task<Location> = fusedLocationClient.lastLocation
        return try {
            Tasks.await(task).toCoordinate()
        } catch (_: Exception) {
            null
        }
    }
}