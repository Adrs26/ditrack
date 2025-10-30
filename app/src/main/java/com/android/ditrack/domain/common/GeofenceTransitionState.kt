package com.android.ditrack.domain.common

sealed class GeofenceTransitionState {
    data object Idle : GeofenceTransitionState()
    data object Enter : GeofenceTransitionState()
    data object Exit : GeofenceTransitionState()
}