package com.android.ditrack.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.ditrack.DitrackApplication
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.repository.UserSessionRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val userSessionRepository by inject<UserSessionRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val appScope = (context.applicationContext as DitrackApplication).applicationScope

        appScope.launch {
            try {
                when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        val firstGeofence = triggeringGeofences.firstOrNull()
                        if (firstGeofence != null) {
                            userSessionRepository.setGeofenceTransition(GeofenceTransition.ENTER)
                            userSessionRepository.setBusStopId(firstGeofence.requestId.toInt())
                            userSessionRepository.setBusStopLocation(
                                LatLng(firstGeofence.latitude, firstGeofence.longitude)
                            )
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        userSessionRepository.setGeofenceTransition(GeofenceTransition.EXIT)
                        userSessionRepository.setBusStopId(-1)
                        userSessionRepository.setBusStopLocation(LatLng(0.0, 0.0))
                    }
                }
            } catch (e: Exception) {
                Log.e("GeofenceReceiver", "Error handling geofence: $e")
            }
        }
    }
}