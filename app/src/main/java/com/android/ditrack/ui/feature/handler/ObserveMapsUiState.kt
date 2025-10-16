package com.android.ditrack.ui.feature.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.domain.model.ApplicationMode
import com.android.ditrack.ui.common.UiState
import com.android.ditrack.ui.feature.components.MapsDialogState
import com.android.ditrack.ui.feature.screen.MapsUiState
import com.android.ditrack.ui.feature.screen.RouteInfoState
import com.android.ditrack.ui.feature.utils.toMessageError
import kotlinx.coroutines.delay

@Composable
fun ObserveMapsUiState(
    mapsUiState: MapsUiState,
    onShowDialog: (MapsDialogState) -> Unit,
    onToggleSheet: (Boolean) -> Unit,
    onShowToast: (String) -> Unit,
    onGetRouteInfo: (RouteInfoState) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(mapsUiState.geofenceTransition) {
        if (
            mapsUiState.geofenceTransition == GeofenceTransition.ENTER &&
            mapsUiState.applicationMode == ApplicationMode.IDLING
        ) {
            delay(5000)
            onShowDialog(MapsDialogState.BusStopNearbyConfirmation)
        }
    }

    LaunchedEffect(mapsUiState.routeInfo) {
        when (val routeInfo = mapsUiState.routeInfo) {
            is UiState.Error -> {
                onShowToast(routeInfo.error.toMessageError().asString(context))
                onShowDialog(MapsDialogState.None)
                onToggleSheet(false)
                onGetRouteInfo(RouteInfoState())
            }
            is UiState.Loading -> {
                onShowDialog(MapsDialogState.Loading)
                onToggleSheet(false)
            }
            is UiState.Success -> {
                when (mapsUiState.applicationMode) {
                    ApplicationMode.DRIVING -> {
                        onShowDialog(MapsDialogState.BusArriveToOriginInformation)
                    }
                    else -> onShowDialog(MapsDialogState.None)
                }
                onToggleSheet(true)
                onGetRouteInfo(routeInfo.data)
            }
            else -> {
                when (mapsUiState.applicationMode) {
                    ApplicationMode.ARRIVING -> {
                        onShowDialog(MapsDialogState.BusArriveToDestinationInformation)
                    }
                    else -> onShowDialog(MapsDialogState.None)
                }
                onToggleSheet(false)
                onGetRouteInfo(RouteInfoState())
            }
        }
    }
}
