package com.android.ditrack.ui.feature.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.ditrack.receiver.GeofenceBroadcastReceiver
import com.android.ditrack.service.LocationTrackingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapsManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geofencingClient: GeofencingClient,
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

    fun addGeofences(busStops: List<BusStopDummy>) {
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

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lng += dlng

            val latLng = LatLng(lat / 1e5, lng / 1e5)
            poly.add(latLng)
        }
        return poly
    }

    fun startLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopLocationTrackingService() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        context.stopService(serviceIntent)
    }

    suspend fun getCurrentGeofenceStatus(
        busStops: List<BusStopDummy>
    ): Pair<Boolean, BusStopDummy> = withContext(Dispatchers.IO) {
        try {
            val location = getLastKnownLocation() ?: return@withContext Pair(false, BusStopDummy())
            val userLatLng = LatLng(location.latitude, location.longitude)

            val inside = busStops.firstOrNull { isInsideGeofence(userLatLng, it) }

            if (inside != null) {
                Pair(true, inside)
            } else {
                Pair(false, BusStopDummy())
            }
        } catch (_: Exception) {
            Pair(false, BusStopDummy())
        }
    }

    private fun getLastKnownLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val task: Task<Location> = fusedLocationClient.lastLocation
        return try {
            Tasks.await(task)
        } catch (_: Exception) {
            null
        }
    }

    private fun isInsideGeofence(
        userLatLng: LatLng,
        busStop: BusStopDummy
    ): Boolean {
        val distance = haversine(
            userLatLng.latitude,
            userLatLng.longitude,
            busStop.latLng.latitude,
            busStop.latLng.longitude
        )
        return distance <= 100f
    }

    private fun haversine(
        userLat: Double,
        userLng: Double,
        busStopLat: Double,
        busStopLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, busStopLat, busStopLng, results)
        return results[0]
    }
}