package com.android.ditrack.data.repository

import com.android.ditrack.data.datastore.UserSessionPreferences
import com.android.ditrack.data.mapper.toCoordinate
import com.android.ditrack.data.mapper.toLatLng
import com.android.ditrack.domain.common.GeofenceTransitionState
import com.android.ditrack.domain.model.Coordinate
import com.android.ditrack.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSessionRepositoryImpl(
    private val userSessionPreferences: UserSessionPreferences
) : UserSessionRepository {

    override fun getGeofenceTransition(): Flow<GeofenceTransitionState> {
        return userSessionPreferences.geofenceTransitionState
    }

    override fun getBusStopId(): Flow<Int> {
        return userSessionPreferences.busStopId
    }

    override fun getBusStopLocation(): Flow<Coordinate> {
        return userSessionPreferences.busStopLocation.map { it.toCoordinate() }
    }

    override fun getBusStopIds(): Flow<List<Int>> {
        return userSessionPreferences.busStopIds
    }

    override suspend fun setGeofenceTransition(transition: GeofenceTransitionState) {
        userSessionPreferences.setGeofenceTransition(transition)
    }

    override suspend fun setBusStopId(id: Int) {
        userSessionPreferences.setBusStopId(id)
    }

    override suspend fun setBusStopLocation(location: Coordinate) {
        userSessionPreferences.setBusStopLocation(location.toLatLng())
    }

    override suspend fun setBusStopIds(ids: List<Int>) {
        userSessionPreferences.setBusStopIds(ids)
    }
}