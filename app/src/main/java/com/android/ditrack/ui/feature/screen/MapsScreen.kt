package com.android.ditrack.ui.feature.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.android.ditrack.R
import com.android.ditrack.ui.feature.utils.MarkerUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    busStops: List<LatLng>
) {
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busStops[0], 15f)
    }
    val mapProperties = MapProperties(
        isTrafficEnabled = true,
        isMyLocationEnabled = locationPermissionState.status.isGranted
    )
    val mapUiSettings = MapUiSettings(
        compassEnabled = true,
        zoomControlsEnabled = false,
        myLocationButtonEnabled = locationPermissionState.status.isGranted
    )

    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded) {
        if (isMapLoaded) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(busStops[0])
                        .zoom(16f)
                        .bearing(0f) // arah kendaraan -> Gunakan bearing dari FusedLocationProvider
                        .tilt(45f) // sudut miring
                        .build()
                )
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLoaded = { isMapLoaded = true }
    ) {
        busStops.forEach { busStop ->
            val markerState = rememberUpdatedMarkerState(busStop)

            Marker(
                state = markerState,
                icon = MarkerUtil.createBusStopMarker(context, R.drawable.ic_store),
                onClick = {
                    // Action when marker is clicked
                    true
                }
            )
        }
    }
}