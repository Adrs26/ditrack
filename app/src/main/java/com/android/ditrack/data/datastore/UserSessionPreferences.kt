package com.android.ditrack.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class UserSessionPreferences(private val context: Context) {

    companion object {
        private val GEOFENCE_TRANSITION_KEY = stringPreferencesKey("geofence_transition")
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
        private val BUS_STOP_LATITUDE_KEY = doublePreferencesKey("bus_stop_latitude")
        private val BUS_STOP_LONGITUDE_KEY = doublePreferencesKey("bus_stop_longitude")
        private val BUS_STOP_IDS_KEY = stringPreferencesKey("bus_stop_ids")
    }

    val geofenceTransition = context.dataStore.data.map { preferences ->
        when (preferences[GEOFENCE_TRANSITION_KEY]) {
            GeofenceTransition.ENTER.name -> GeofenceTransition.ENTER
            GeofenceTransition.EXIT.name -> GeofenceTransition.EXIT
            else -> GeofenceTransition.DEFAULT
        }
    }

    val busStopId = context.dataStore.data.map { preferences ->
        preferences[BUS_STOP_ID_KEY] ?: -1
    }

    val busStopLocation = context.dataStore.data.map { preferences ->
        LatLng(preferences[BUS_STOP_LATITUDE_KEY] ?: 0.0, preferences[BUS_STOP_LONGITUDE_KEY] ?: 0.0)
    }

    val busStopIds = context.dataStore.data.map { preferences ->
        preferences[BUS_STOP_IDS_KEY].let {
            Json.decodeFromString<List<Int>>(it ?: "[]")
        }
    }

    suspend fun setGeofenceTransition(transition: GeofenceTransition) {
        context.dataStore.edit { preferences ->
            preferences[GEOFENCE_TRANSITION_KEY] = transition.name
        }
    }

    suspend fun setBusStopId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_ID_KEY] = id
        }
    }

    suspend fun setBusStopLocation(location: LatLng) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_LATITUDE_KEY] = location.latitude
            preferences[BUS_STOP_LONGITUDE_KEY] = location.longitude
        }
    }

    suspend fun setBusStopIds(ids: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_IDS_KEY] = Json.encodeToString(ids)
        }
    }
}

enum class GeofenceTransition {
    DEFAULT, ENTER, EXIT
}