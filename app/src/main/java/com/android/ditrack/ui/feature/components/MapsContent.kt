package com.android.ditrack.ui.feature.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DepartureBoard
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.ditrack.R
import com.android.ditrack.ui.feature.utils.BusStopDummy
import com.android.ditrack.ui.feature.utils.MarkerUtil
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsContent(
    context: Context,
    busStops: List<BusStopDummy>,
    cameraPositionState: CameraPositionState,
    isLocationPermissionGranted: Boolean,
    isMapLoaded: Boolean,
    isSheetVisible: Boolean,
    polyLinePoints: List<LatLng>,
    onMapLoaded: () -> Unit,
    onAnimateToMyLocationClick: () -> Unit,
    onStartTrackingClick: () -> Unit
) {
    Box {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isTrafficEnabled = true,
                isMyLocationEnabled = isLocationPermissionGranted
            ),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapLoaded = onMapLoaded
        ) {
            busStops.forEach { busStop ->
                val markerState = rememberUpdatedMarkerState(busStop.latLng)

                Marker(
                    state = markerState,
                    icon = MarkerUtil.createBusStopMarker(context, R.drawable.ic_store),
                    onClick = {
                        // Action when marker is clicked
                        true
                    }
                )
            }

            Marker(
                state = rememberUpdatedMarkerState(LatLng(-7.0550504, 110.4428640)),
                icon = MarkerUtil.createBusMarker(context, R.drawable.ic_directions_bus),
                onClick = {
                    // Action when marker is clicked
                    true
                }
            )

            if (polyLinePoints.isNotEmpty()) {
                Polyline(
                    points = polyLinePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 10f
                )
            }
        }
        if (isMapLoaded) {
            FloatingActionButton(
                onClick = onAnimateToMyLocationClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(36.dp),
                shape = MaterialTheme.shapes.extraSmall,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null
                )
            }
        }
        if (isMapLoaded && !isSheetVisible) {
            FloatingActionButton(
                onClick = onStartTrackingClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.DepartureBoard,
                    contentDescription = null
                )
            }
        }
    }
}