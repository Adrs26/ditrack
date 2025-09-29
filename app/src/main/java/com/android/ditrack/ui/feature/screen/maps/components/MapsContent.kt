package com.android.ditrack.ui.feature.screen.maps.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.ditrack.R
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.ui.feature.utils.MarkerUtil
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsContent(
    context: Context,
    cameraPositionState: CameraPositionState,
    isLocationPermissionGranted: Boolean,
    busStops: List<LatLng>,
    isMapLoaded: Boolean,
    showSheet: Boolean,
    onMapLoaded: () -> Unit,
    onModeChange: (ApplicationMode) -> Unit,
    onNavigateToMyLocation: () -> Unit,
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
        if (isMapLoaded) {
            FloatingActionButton(
                onClick = onNavigateToMyLocation,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(36.dp),
                shape = MaterialTheme.shapes.extraSmall,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_my_location),
                    contentDescription = null
                )
            }
        }
        if (isMapLoaded && !showSheet) {
            FloatingActionButton(
                onClick = { onModeChange(ApplicationMode.WAITING) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_departure_board),
                    contentDescription = null
                )
            }
        }
    }
}