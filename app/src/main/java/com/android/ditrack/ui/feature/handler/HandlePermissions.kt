package com.android.ditrack.ui.feature.handler

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.android.ditrack.R
import com.android.ditrack.ui.feature.utils.showMessageWithToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun handlePermissions(
    isMapLoaded: Boolean,
    onMapReady: (Boolean, Boolean) -> Unit
): PermissionState {
    val context = LocalContext.current

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = { isGranted ->
            onMapReady(isGranted, isMapLoaded)
            if (isGranted) {
                notificationPermissionState?.launchPermissionRequest()
            } else {
                context.getString(R.string.access_fine_location_required_to_use_application_service)
                    .showMessageWithToast(context)
            }
        }
    )

    LaunchedEffect(isMapLoaded) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            onMapReady(true, isMapLoaded)
        }
    }

    return locationPermissionState
}