package com.android.ditrack.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.ditrack.DitrackApplication
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.data.datastore.UserSessionPreferences
import com.android.ditrack.ui.feature.utils.NotificationUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val userSessionPrefs: UserSessionPreferences by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val geofenceTransition = geofencingEvent.geofenceTransition
        val appScope = (context.applicationContext as DitrackApplication).applicationScope

        appScope.launch {
            try {
                val currentMode = userSessionPrefs.applicationMode.first()
                val busStopId = userSessionPrefs.busStopId.first()

                when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        if (currentMode == ApplicationMode.DEFAULT) {
                            geofencingEvent.triggeringGeofences?.forEach { geofence ->
                                userSessionPrefs.setGeofenceTransition(GeofenceTransition.ENTER)
                                userSessionPrefs.setBusStopId(geofence.requestId.toIntOrNull() ?: 0)
                            }
                            NotificationUtil.sendNotification(
                                context,
                                "Halte terdeteksi",
                                "Posisi anda berada di area halte bus"
                            )
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        if (currentMode == ApplicationMode.WAITING) {
                            userSessionPrefs.setGeofenceTransition(GeofenceTransition.EXIT)
                            NotificationUtil.sendNotification(
                                context,
                                "Halte tidak terdeteksi",
                                "Anda berada di luar area halte bus $busStopId"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GeofenceReceiver", "Error handling geofence: $e")
            }
        }
    }
}