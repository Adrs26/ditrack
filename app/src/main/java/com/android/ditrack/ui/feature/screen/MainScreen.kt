package com.android.ditrack.ui.feature.screen

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.ditrack.R
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.ui.feature.components.BusStopListContent
import com.android.ditrack.ui.feature.components.ConfirmationDialog
import com.android.ditrack.ui.feature.components.MapsContent
import com.android.ditrack.ui.feature.components.SheetContent
import com.android.ditrack.ui.feature.components.SheetHandle
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.showMessageWithToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    cameraUpdateEvent: SharedFlow<CameraUpdate>,
    onLocationPermissionResult: (Boolean, Boolean) -> Unit,
    onAnimateToMyLocationClick: () -> Unit,
    onModeChange: (ApplicationMode) -> Unit
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isMapLoaded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isSheetVisible by remember { mutableStateOf(false) }
    var isBusStopListVisible by remember { mutableStateOf(false) }

    Log.d("MainScreen", "uiState: $uiState")

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else { null }

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = { isGranted ->
            onLocationPermissionResult(isGranted, isMapLoaded)
            if (isGranted) {
                notificationPermissionState?.launchPermissionRequest()
            } else {
                context.getString(R.string.access_fine_location_required_to_use_application_service)
                    .showMessageWithToast(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        cameraUpdateEvent.collect { cameraUpdate ->
            cameraPositionState.animate(update = cameraUpdate, durationMs = 1000)
        }
    }

    LaunchedEffect(isMapLoaded) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            onLocationPermissionResult(true, isMapLoaded)
        }
    }

    LaunchedEffect(uiState.geofenceTransition) {
        if (
            uiState.geofenceTransition == GeofenceTransition.ENTER &&
            uiState.applicationMode == ApplicationMode.DEFAULT
        ) {
            isDialogVisible = true
        }
    }

    LaunchedEffect(uiState.applicationMode) {
        if (uiState.applicationMode != ApplicationMode.DEFAULT) {
            isSheetVisible = true
        }
    }

    if (locationPermissionState.status.isGranted) {
        BottomSheetScaffold(
            sheetContent = {
                SheetContent(
                    applicationMode = uiState.applicationMode,
                    onModeChange = { newMode ->
                        onModeChange(newMode)
                        isSheetVisible = newMode != ApplicationMode.DEFAULT
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = if (isSheetVisible) SheetValue.PartiallyExpanded else SheetValue.Hidden,
                    skipHiddenState = isSheetVisible
                )
            ),
            sheetPeekHeight = 152.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetShadowElevation = 8.dp,
            sheetDragHandle = { SheetHandle(modifier = Modifier.padding(vertical = 16.dp)) }
        ) {
            MapsContent(
                context = context,
                busStops = DataDummyProvider.getBusStops(),
                cameraPositionState = cameraPositionState,
                isLocationPermissionGranted = locationPermissionState.status.isGranted,
                isMapLoaded = isMapLoaded,
                isSheetVisible = isSheetVisible,
                onMapLoaded = { isMapLoaded = true },
                onAnimateToMyLocationClick = onAnimateToMyLocationClick,
                onStartTrackingClick = {
                    if (
                        uiState.geofenceTransition == GeofenceTransition.ENTER ||
                        uiState.geofenceTransition == GeofenceTransition.DWELL
                    ) {
                        isBusStopListVisible = true
                    } else {
                        "Fitur hanya bisa digunakan ketika berada di area halte"
                            .showMessageWithToast(context)
                    }
                }
            )
            AnimatedVisibility(
                visible = isBusStopListVisible,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                BusStopListContent(
                    busStops = DataDummyProvider.getBusStops(),
                    onNavigateBack = { isBusStopListVisible = false },
                    onBusStopSelect = { onModeChange(it) }
                )
            }
        }
    }

    if (isMapLoaded && isDialogVisible) {
        val busStopName = DataDummyProvider.getBusStops()
            .firstOrNull { it.id == uiState.busStopId }?.name ?: ""

        ConfirmationDialog(
            icon = R.drawable.ic_departure_board,
            title = "Halte terdeteksi",
            description = "Posisi kamu berada di area ${busStopName}. Ingin menunggu di halte ini?",
            onDismissRequest = { isDialogVisible = false },
            onConfirmRequest = {
                isDialogVisible = false
                isBusStopListVisible = true
            }
        )
    }

    BackHandler(isBusStopListVisible) {
        isBusStopListVisible = false
    }
}