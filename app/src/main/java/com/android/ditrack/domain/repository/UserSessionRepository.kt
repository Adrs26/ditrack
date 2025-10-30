package com.android.ditrack.domain.repository

import com.android.ditrack.domain.common.GeofenceTransitionState
import com.android.ditrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

interface UserSessionRepository {

    fun getGeofenceTransition(): Flow<GeofenceTransitionState>
    fun getBusStopId(): Flow<Int>
    fun getBusStopLocation(): Flow<Coordinate>
    fun getBusStopIds(): Flow<List<Int>>

    suspend fun setGeofenceTransition(transition: GeofenceTransitionState)
    suspend fun setBusStopId(id: Int)
    suspend fun setBusStopLocation(location: Coordinate)
    suspend fun setBusStopIds(ids: List<Int>)
}
