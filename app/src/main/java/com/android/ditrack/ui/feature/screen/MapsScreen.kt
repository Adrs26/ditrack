package com.android.ditrack.ui.feature.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.ditrack.R
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.ui.common.UiState
import com.android.ditrack.ui.feature.components.BusInformationDialog
import com.android.ditrack.ui.feature.components.BusStopInformationDialog
import com.android.ditrack.ui.feature.components.BusStopListContent
import com.android.ditrack.ui.feature.components.ConfirmationDialog
import com.android.ditrack.ui.feature.components.LoadingDialog
import com.android.ditrack.ui.feature.components.MapsContent
import com.android.ditrack.ui.feature.components.SheetContent
import com.android.ditrack.ui.feature.components.SheetHandle
import com.android.ditrack.ui.feature.utils.DataDummyProvider
import com.android.ditrack.ui.feature.utils.showMessageWithToast
import com.android.ditrack.ui.feature.utils.toMessageError
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
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
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    val polyLinePoints = remember(mapsUiState.routeInfo) {
        when (mapsUiState.routeInfo) {
            is UiState.Success -> mapsUiState.routeInfo.data.polylinePoints
            else -> emptyList()
        }
    }
    val duration = remember(mapsUiState.routeInfo) {
        when (mapsUiState.routeInfo) {
            is UiState.Success -> mapsUiState.routeInfo.data.duration
            else -> ""
        }
    }
    val distance = remember(mapsUiState.routeInfo) {
        when (mapsUiState.routeInfo) {
            is UiState.Success -> mapsUiState.routeInfo.data.distance
            else -> ""
        }
    }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else { null }

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = { isGranted ->
            mapsActions.onMapReady(isGranted, isMapLoaded)
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
            mapsActions.onMapReady(true, isMapLoaded)
        }
    }

    LaunchedEffect(mapsUiState.geofenceTransition) {
        if (
            mapsUiState.geofenceTransition == GeofenceTransition.ENTER &&
            mapsUiState.applicationMode == ApplicationMode.DEFAULT
        ) {
            delay(5000)
            dialogState = DialogState.BusStopNearbyConfirmation
        }
    }

    LaunchedEffect(mapsUiState.applicationMode) {
        if (mapsUiState.applicationMode != ApplicationMode.DEFAULT) {
            isSheetVisible = true
        }
    }

    LaunchedEffect(mapsUiState.routeInfo) {
        if (mapsUiState.routeInfo is UiState.Error) {
            mapsUiState.routeInfo.error.toMessageError().asString(context)
                .showMessageWithToast(context)
        }

        isSheetVisible = when (mapsUiState.routeInfo) {
            is UiState.Success -> true
            else -> false
        }

        dialogState = if (mapsUiState.routeInfo is UiState.Loading) {
            DialogState.Loading
        } else {
            DialogState.None
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mapsActions.onResumeAction(locationPermissionState.status.isGranted, isMapLoaded)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (locationPermissionState.status.isGranted) {
        BottomSheetScaffold(
            sheetContent = {
                SheetContent(
                    applicationMode = mapsUiState.applicationMode,
                    originName = mapsUiState.busStopOriginName,
                    destinationName = mapsUiState.busStopDestinationName,
                    duration = duration,
                    distance = distance,
                    onStartDriving = { dialogState = DialogState.BusArriveToOriginConfirmation },
                    onExitWaiting = mapsActions::onStopWaiting,
                    onChangeDestination = { isBusStopListVisible = true },
                    onFinishTrip = { dialogState = DialogState.BusArriveToDestinationConfirmation },
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
                polyLinePoints = polyLinePoints,
                onMapLoaded = { isMapLoaded = true },
                onBusStopMarkerClick = { dialogState = DialogState.BusStopInformation },
                onBusMarkerClick = { dialogState = DialogState.BusInformation },
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

    when (dialogState) {
        DialogState.None -> {}
        DialogState.Loading -> {
            LoadingDialog()
        }
        DialogState.BusInformation -> {
            BusInformationDialog(
                onDismissRequest = { dialogState = DialogState.None }
            )
        }
        DialogState.BusStopInformation -> {
            BusStopInformationDialog(
                onDismissRequest = { dialogState = DialogState.None }
            )
        }
        DialogState.BusArriveToOriginConfirmation -> {
            ConfirmationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.enter_driving_mode_confirmation,
                    mapsUiState.busStopOriginName
                ),
                onDismissRequest = { dialogState = DialogState.None },
                onConfirmRequest = {
                    mapsActions.onStartDriving()
                    dialogState = DialogState.None
                    isBusStopListVisible = false
                }
            )
        }
        DialogState.BusArriveToDestinationConfirmation -> {
            ConfirmationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.finish_trip_confirmation,
                    mapsUiState.busStopDestinationName
                ),
                onDismissRequest = { dialogState = DialogState.None },
                onConfirmRequest = {
                    mapsActions.onStopWaiting()
                    dialogState = DialogState.None
                    isBusStopListVisible = false
                }
            )
        }
        DialogState.BusStopNearbyConfirmation -> {
            if (isMapLoaded) {
                ConfirmationDialog(
                    icon = Icons.Default.Store,
                    title = stringResource(R.string.bus_stop_detected),
                    description = stringResource(
                        R.string.enter_waiting_mode_confirmation,
                        mapsUiState.busStopOriginName
                    ),
                    onDismissRequest = { dialogState = DialogState.None },
                    onConfirmRequest = {
                        dialogState = DialogState.None
                        isBusStopListVisible = true
                    }
                )
            }
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
    fun onStartDriving()
    fun onResumeAction(isGranted: Boolean, isMapLoaded: Boolean)
}

sealed class DialogState {
    object None : DialogState()
    object Loading : DialogState()
    object BusInformation : DialogState()
    object BusStopInformation : DialogState()
    object BusArriveToOriginConfirmation : DialogState()
    object BusArriveToDestinationConfirmation : DialogState()
    object BusStopNearbyConfirmation : DialogState()
}