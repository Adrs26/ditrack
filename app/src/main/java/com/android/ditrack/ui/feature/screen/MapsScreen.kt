package com.android.ditrack.ui.feature.screen

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.ditrack.R
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.ui.feature.components.BusStopListContent
import com.android.ditrack.ui.feature.components.DialogSection
import com.android.ditrack.ui.feature.components.MapsContent
import com.android.ditrack.ui.feature.components.MapsDialogState
import com.android.ditrack.ui.feature.components.SheetContent
import com.android.ditrack.ui.feature.components.SheetHandle
import com.android.ditrack.ui.feature.handler.CollectMapsEvent
import com.android.ditrack.ui.feature.handler.ObserveMapsUiState
import com.android.ditrack.ui.feature.handler.handlePermissions
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.showMessageWithToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    cameraUpdateEvent: Flow<CameraUpdate>,
    mapsUiState: MapsUiState,
    mapsActions: MapsActions,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isMapLoaded by remember { mutableStateOf(false) }
    var isSheetVisible by remember { mutableStateOf(false) }
    var isBusStopListVisible by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<MapsDialogState>(MapsDialogState.None) }

    val locationPermissionState = handlePermissions(
        isMapLoaded = isMapLoaded,
        onMapReady = mapsActions::onMapReady
    )

    var routeInfoState by remember { mutableStateOf(RouteInfoState()) }

    ObserveMapsUiState(
        mapsUiState = mapsUiState,
        onShowDialog = { dialogState = it },
        onToggleSheet = { isSheetVisible = it },
        onShowToast = { it.showMessageWithToast(context) },
        onGetRouteInfo = { routeInfoState = it }
    )

    CollectMapsEvent(
        cameraUpdateEvent = cameraUpdateEvent,
        cameraPositionState = cameraPositionState
    )

    if (locationPermissionState.status.isGranted) {
        BottomSheetScaffold(
            sheetContent = {
                SheetContent(
                    applicationMode = mapsUiState.applicationMode,
                    originName = mapsUiState.busStopOriginName,
                    destinationName = mapsUiState.busStopDestinationName,
                    duration = routeInfoState.duration,
                    distance = routeInfoState.distance,
                    onExitWaiting = mapsActions::onStopWaiting,
                    onChangeDestination = { isBusStopListVisible = true },
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
                busStops = mapsUiState.busStops,
                cameraPositionState = cameraPositionState,
                isLocationPermissionGranted = locationPermissionState.status.isGranted,
                isMapLoaded = isMapLoaded,
                isSheetVisible = isSheetVisible,
                polyLinePoints = routeInfoState.polylinePoints,
                onMapLoaded = { isMapLoaded = true },
                onBusStopMarkerClick = { dialogState = MapsDialogState.BusStopInformation },
                onBusMarkerClick = { dialogState = MapsDialogState.BusInformation },
                onAnimateToMyLocationClick = mapsActions::onAnimateToUserLocation,
                onStartTrackingClick = {
                    if (mapsUiState.geofenceTransition == GeofenceTransition.ENTER) {
                        isBusStopListVisible = true
                    } else {
                        context.getString(R.string.feature_only_available_at_the_bus_stop_area)
                            .showMessageWithToast(context)
                    }
                }
            )
        }
    }

    AnimatedVisibility(
        visible = isBusStopListVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        BusStopListContent(
            busStops = DataDummyProvider.getBusStops(),
            onNavigateBack = { isBusStopListVisible = false },
            onBusStopSelect = mapsActions::onStartWaiting
        )
    }

    DialogSection(
        dialogState = dialogState,
        mapsUiState = mapsUiState,
        isMapLoaded = isMapLoaded,
        onShowBusStopList = { isBusStopListVisible = it },
        onDismissRequest = { dialogState = MapsDialogState.None }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mapsActions.onResume(locationPermissionState.status.isGranted, isMapLoaded)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(isBusStopListVisible) {
        isBusStopListVisible = false
    }
}

interface MapsActions {
    fun onMapReady(isGranted: Boolean, isMapLoaded: Boolean)
    fun onAnimateToUserLocation()
    fun onStartWaiting(destinationName: String, destinationLocation: LatLng)
    fun onStopWaiting()
    fun onResume(isGranted: Boolean, isMapLoaded: Boolean)
}