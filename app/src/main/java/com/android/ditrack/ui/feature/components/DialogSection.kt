package com.android.ditrack.ui.feature.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.ditrack.R
import com.android.ditrack.ui.feature.screen.MapsUiState

@Composable
fun DialogSection(
    dialogState: MapsDialogState,
    mapsUiState: MapsUiState,
    isMapLoaded: Boolean,
    onShowBusStopList: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        MapsDialogState.None -> {}
        MapsDialogState.Loading -> {
            LoadingDialog()
        }
        MapsDialogState.BusInformation -> {
            BusInformationDialog(
                onDismissRequest = onDismissRequest
            )
        }
        MapsDialogState.BusStopInformation -> {
            BusStopInformationDialog(
                onDismissRequest = onDismissRequest
            )
        }
        MapsDialogState.BusArriveToOriginInformation -> {
            InformationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.enter_driving_mode_confirmation,
                    mapsUiState.busStopOriginName
                ),
                onDismissRequest = onDismissRequest
            )
        }
        MapsDialogState.BusArriveToDestinationInformation -> {
            InformationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.finish_trip_confirmation,
                    mapsUiState.busStopDestinationName
                ),
                onDismissRequest = onDismissRequest
            )
        }
        MapsDialogState.BusStopNearbyConfirmation -> {
            if (isMapLoaded) {
                ConfirmationDialog(
                    icon = Icons.Default.Store,
                    title = stringResource(R.string.bus_stop_detected),
                    description = stringResource(
                        R.string.enter_waiting_mode_confirmation,
                        mapsUiState.busStopOriginName
                    ),
                    onDismissRequest = onDismissRequest,
                    onConfirmRequest = {
                        onDismissRequest()
                        onShowBusStopList(true)
                    }
                )
            }
        }
    }
}

sealed class MapsDialogState {
    object None : MapsDialogState()
    object Loading : MapsDialogState()
    object BusInformation : MapsDialogState()
    object BusStopInformation : MapsDialogState()
    object BusArriveToOriginInformation : MapsDialogState()
    object BusArriveToDestinationInformation : MapsDialogState()
    object BusStopNearbyConfirmation : MapsDialogState()
}