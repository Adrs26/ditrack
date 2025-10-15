package com.android.ditrack.domain.repository

import com.android.ditrack.data.datastore.GeofenceTransition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface UserSessionRepository {

    fun getGeofenceTransition(): Flow<GeofenceTransition>
    fun getBusStopId(): Flow<Int>
    fun getBusStopLocation(): Flow<LatLng>
    fun getBusStopIds(): Flow<List<Int>>

    suspend fun setGeofenceTransition(transition: GeofenceTransition)
    suspend fun setBusStopId(id: Int)
    suspend fun setBusStopLocation(location: LatLng)
    suspend fun setBusStopIds(ids: List<Int>)
}
