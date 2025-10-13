package com.android.ditrack.ui.feature.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdate
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.flow.Flow

@Composable
fun CollectMapsEvent(
    cameraUpdateEvent: Flow<CameraUpdate>,
    cameraPositionState: CameraPositionState
) {
    LaunchedEffect(Unit) {
        cameraUpdateEvent.collect { cameraUpdate ->
            cameraPositionState.animate(update = cameraUpdate, durationMs = 1000)
        }
    }
}