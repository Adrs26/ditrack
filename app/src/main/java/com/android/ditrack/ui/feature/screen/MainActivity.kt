package com.android.ditrack.ui.feature.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.ditrack.ui.feature.utils.NotificationUtil
import com.android.ditrack.ui.theme.DitrackTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationUtil.createNotificationChannel(this)

        setContent {
            DitrackTheme {
                val viewModel = koinViewModel<MainViewModel>()
                val cameraUpdateEvent = viewModel.cameraUpdateEvent
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                MainScreen(
                    uiState = uiState,
                    cameraUpdateEvent = cameraUpdateEvent,
                    onLocationPermissionResult = viewModel::syncGeofence,
                    onAnimateToMyLocationClick = viewModel::animateToUserLocation,
                    onModeChange = viewModel::setApplicationMode
                )
            }
        }
    }
}