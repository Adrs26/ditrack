package com.android.ditrack.ui.feature.screen

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import com.android.ditrack.R
import com.android.ditrack.ui.feature.utils.GeofenceBroadcastReceiver
import com.android.ditrack.ui.feature.utils.GeofenceEventBus
import com.android.ditrack.ui.feature.utils.MarkerUtil
import com.android.ditrack.ui.theme.SoftWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
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
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else { null }

    val backgroundLocationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            onPermissionResult = {
                if (it) {
                    notificationPermissionState?.launchPermissionRequest()
                }
            }
        )
    } else { null }

    val foregroundLocationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = {
            if (it) {
                backgroundLocationPermissionState?.launchPermissionRequest()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!foregroundLocationPermissionState.status.isGranted) {
            foregroundLocationPermissionState.launchPermissionRequest()
        }
    }

    val context = LocalContext.current
    val geofencingClient = remember { LocationServices.getGeofencingClient(context) }

    var showDialog by remember { mutableStateOf(false) }
    var busStop by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busStops[0], 15f)
    }
    val mapProperties = MapProperties(
        isTrafficEnabled = true,
        isMyLocationEnabled = foregroundLocationPermissionState.status.isGranted
    )
    val mapUiSettings = MapUiSettings(
        compassEnabled = false,
        zoomControlsEnabled = false,
        myLocationButtonEnabled = false
    )

    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded) {
        if (isMapLoaded) {
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newCameraPosition(
//                    CameraPosition.Builder()
//                        .target(busStops[0])
//                        .zoom(16f)
//                        .bearing(0f) // bus direction -> Use bearing from FusedLocationProvider
//                        .tilt(45f) // map tilt
//                        .build()
//                )
//            )
            addGeofences(context, busStops, geofencingClient)
        }
    }

    LaunchedEffect(GeofenceEventBus.events) {
        GeofenceEventBus.events.collect {
            busStop = it
            showDialog = true
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

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Anda berada di dekat halte $busStop")
                }
            }
        }
    }
}

private fun addGeofences(
    context: Context,
    busStops: List<LatLng>,
    geofencingClient: GeofencingClient,
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val geofences = mutableListOf<Geofence>()
    busStops.forEachIndexed { index, busStop ->
        geofences.add(
            Geofence.Builder()
                .setRequestId(index.toString())
                .setCircularRegion(busStop.latitude, busStop.longitude, 500f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        )
    }

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofences(geofences)
        .build()

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
}