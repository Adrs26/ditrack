package com.android.ditrack.ui.feature.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.ditrack.DitrackApplication
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.launch

open class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val geofenceTransition = geofencingEvent.geofenceTransition
        val appScope = (context.applicationContext as DitrackApplication).applicationScope

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            appScope.launch {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                if (triggeringGeofences != null) {
                    for (geofence in triggeringGeofences) {
                        GeofenceEventBus.postEvent("Halte ${geofence.requestId} terdeteksi")
                        NotificationUtil.sendNotification(
                            context,
                            "Halte ${geofence.requestId} terdeteksi",
                            "Anda telah berada di halte bus ${geofence.requestId}"
                        )
                    }
                }
            }
        }
    }
}