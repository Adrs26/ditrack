package com.android.ditrack.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class UserSessionPreferences(private val context: Context) {

    companion object {
        private val GEOFENCE_TRANSITION_KEY = stringPreferencesKey("geofence_transition")
        private val APPLICATION_MODE_KEY = stringPreferencesKey("application_mode")
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
    }

    val geofenceTransition = context.dataStore.data.map { preferences ->
        when (preferences[GEOFENCE_TRANSITION_KEY]) {
            GeofenceTransition.ENTER.name -> GeofenceTransition.ENTER
            GeofenceTransition.EXIT.name -> GeofenceTransition.EXIT
            else -> GeofenceTransition.DEFAULT
        }
    }

    val applicationMode = context.dataStore.data.map { preferences ->
        when (preferences[APPLICATION_MODE_KEY]) {
            ApplicationMode.WAITING.name -> ApplicationMode.WAITING
            ApplicationMode.DRIVING.name -> ApplicationMode.DRIVING
            ApplicationMode.ARRIVED.name -> ApplicationMode.ARRIVED
            else -> ApplicationMode.DEFAULT
        }
    }

    val busStopId = context.dataStore.data.map { preferences ->
        preferences[BUS_STOP_ID_KEY] ?: 0
    }

    suspend fun setGeofenceTransition(transition: GeofenceTransition) {
        context.dataStore.edit { preferences ->
            preferences[GEOFENCE_TRANSITION_KEY] = transition.name
        }
    }

    suspend fun setApplicationMode(mode: ApplicationMode) {
        context.dataStore.edit { preferences ->
            preferences[APPLICATION_MODE_KEY] = mode.name
        }
    }

    suspend fun setBusStopId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_ID_KEY] = id
        }
    }

    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

enum class GeofenceTransition {
    DEFAULT, ENTER, EXIT
}

enum class ApplicationMode {
    DEFAULT, WAITING, DRIVING, ARRIVED
}