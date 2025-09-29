package com.android.ditrack.ui.feature.screen.maps

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.ditrack.R
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.service.LocationTrackingService
import com.android.ditrack.ui.feature.components.ConfirmationDialog
import com.android.ditrack.ui.feature.screen.maps.components.MapsContent
import com.android.ditrack.ui.feature.screen.maps.components.SheetContent
import com.android.ditrack.ui.feature.utils.MapsUtil
import com.android.ditrack.ui.feature.utils.showMessageWithToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    busStops: List<LatLng>,
    applicationMode: ApplicationMode,
    geofenceTransition: GeofenceTransition,
    busStopId: Int,
    onModeChange: (ApplicationMode) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geofencingClient = remember { LocationServices.getGeofencingClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }
    val serviceIntent = Intent(context, LocationTrackingService::class.java)

    var isMapReady by remember { mutableStateOf(false) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = { isMapReady = true }
        )
    } else { null }

    val foregroundLocationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = {
            if (it) {
                notificationPermissionState?.launchPermissionRequest()
            } else {
                context.getString(R.string.access_fine_location_required_to_use_application_service)
                    .showMessageWithToast(context)
                isMapReady = true
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!foregroundLocationPermissionState.status.isGranted) {
            foregroundLocationPermissionState.launchPermissionRequest()
        } else {
            isMapReady = true
        }
    }

    LaunchedEffect(isMapLoaded) {
        if (isMapLoaded && foregroundLocationPermissionState.status.isGranted) {
            MapsUtil.getCurrentLatLng(context, fusedLocationClient) { latLng ->
                if (latLng != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)
                }
            }
            MapsUtil.addGeofences(context, busStops, geofencingClient)
        } else if (!foregroundLocationPermissionState.status.isGranted) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(busStops[0], 16f)
        }
    }

    LaunchedEffect(geofenceTransition) {
        if (geofenceTransition == GeofenceTransition.ENTER) { showDialog = true }
    }

    if (isMapReady) {
        BottomSheetScaffold(
            sheetContent = {
                SheetContent(
                    applicationMode = applicationMode,
                    onModeChange = { applicationMode ->
                        MapsUtil.getCurrentLatLng(context, fusedLocationClient) { latLng ->
                            if (latLng != null) {
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                                        durationMs = 1000
                                    )
                                }
                            }
                        }
                        onModeChange(applicationMode)

                        if (applicationMode == ApplicationMode.DRIVING) {
                            showSheet = true
                        } else {
                            showSheet = false
                            context.stopService(serviceIntent)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = if (showSheet) SheetValue.PartiallyExpanded else SheetValue.Hidden,
                    skipHiddenState = showSheet
                )
            ),
            sheetPeekHeight = 152.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetShadowElevation = 8.dp,
            sheetDragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp, 4.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        ) {
            MapsContent(
                context = context,
                cameraPositionState = cameraPositionState,
                isLocationPermissionGranted = foregroundLocationPermissionState.status.isGranted,
                busStops = busStops,
                isMapLoaded = isMapLoaded,
                showSheet = applicationMode == ApplicationMode.WAITING,
                onMapLoaded = { isMapLoaded = true },
                onModeChange = { applicationMode ->
                    showSheet = true
                    MapsUtil.getCurrentLatLng(context, fusedLocationClient) { latLng ->
                        if (latLng != null) {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                                    durationMs = 1000
                                )
                            }
                        }
                    }
                    onModeChange(applicationMode)
                    ContextCompat.startForegroundService(context, serviceIntent)
                },
                onNavigateToMyLocation = {
                    MapsUtil.getCurrentLatLng(context, fusedLocationClient) { latLng ->
                        if (latLng != null) {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                                    durationMs = 1000
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    if (isMapLoaded && showDialog) {
        ConfirmationDialog(
            icon = R.drawable.ic_departure_board,
            title = "Halte terdeteksi",
            description = "Posisi kamu berada di area halte $busStopId. Ingin menunggu di halte ini?",
            onDismissRequest = { showDialog = false },
            onConfirmRequest = { showDialog = false }
        )
    }
}