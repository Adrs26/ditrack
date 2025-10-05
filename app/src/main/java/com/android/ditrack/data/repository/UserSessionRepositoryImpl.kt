package com.android.ditrack.data.repository

import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.data.datastore.UserSessionPreferences
import com.android.ditrack.domain.repository.UserSessionRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

class UserSessionRepositoryImpl(
    private val userSessionPreferences: UserSessionPreferences
) : UserSessionRepository {

    override fun getGeofenceTransition(): Flow<GeofenceTransition> {
        return userSessionPreferences.geofenceTransition
    }

    override fun getApplicationMode(): Flow<ApplicationMode> {
        return userSessionPreferences.applicationMode
    }

    override fun getBusStopId(): Flow<Int> {
        return userSessionPreferences.busStopId
    }

    override fun getBusStopLocation(): Flow<LatLng> {
        return userSessionPreferences.busStopLocation
    }

    override fun getBusStopIds(): Flow<List<Int>> {
        return userSessionPreferences.busStopIds
    }

    override suspend fun setGeofenceTransition(transition: GeofenceTransition) {
        userSessionPreferences.setGeofenceTransition(transition)
    }

    override suspend fun setApplicationMode(mode: ApplicationMode) {
        userSessionPreferences.setApplicationMode(mode)
    }

    override suspend fun setBusStopId(id: Int) {
        userSessionPreferences.setBusStopId(id)
    }

    override suspend fun setBusStopLocation(location: LatLng) {
        userSessionPreferences.setBusStopLocation(location)
    }

    override suspend fun setBusStopIds(ids: List<Int>) {
        userSessionPreferences.setBusStopIds(ids)
    }
}