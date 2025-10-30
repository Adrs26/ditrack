package com.android.ditrack.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.ditrack.DitrackApplication
import com.android.ditrack.domain.common.GeofenceTransitionState
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.repository.UserSessionRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
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
                            userSessionRepository.setGeofenceTransition(GeofenceTransitionState.Enter)
                            userSessionRepository.setBusStopId(firstGeofence.requestId.toInt())
                            userSessionRepository.setBusStopLocation(
                                Coordinate(firstGeofence.latitude, firstGeofence.longitude)
                            )
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        userSessionRepository.setGeofenceTransition(GeofenceTransitionState.Exit)
                        userSessionRepository.setBusStopId(-1)
                        userSessionRepository.setBusStopLocation(Coordinate(0.0, 0.0))
                    }
                }
            } catch (_: Exception) {}
        }
    }
}