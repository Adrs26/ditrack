package com.android.ditrack.ui.feature.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.ditrack.ui.feature.utils.NotificationUtil
import com.android.ditrack.ui.theme.DitrackTheme
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationUtil.createNotificationChannel(this)

        setContent {
            DitrackTheme {
                val viewModel = koinViewModel<MapsViewModel>()
                val cameraUpdateEvent = viewModel.cameraUpdateEvent
                val mapsUiState by viewModel.mapsUiState.collectAsStateWithLifecycle()

                val mapsActions = object : MapsActions {
                    override fun onMapReady(isGranted: Boolean, isMapLoaded: Boolean) {
                        viewModel.syncGeofence(
                            isGranted = isGranted,
                            isMapLoaded = isMapLoaded
                        )
                    }

                    override fun onAnimateToUserLocation() {
                        viewModel.animateToUserLocation()
                    }

                    override fun onStartWaiting(
                        destinationName: String,
                        destinationLocation: LatLng
                    ) {
                        viewModel.startWaitingMode(
                            destinationName = destinationName,
                            destinationLocation = destinationLocation
                        )
                    }

                    override fun onStopWaiting() {
                        viewModel.stopWaitingMode()
                    }

                    override fun onResume(isGranted: Boolean, isMapLoaded: Boolean) {
                        viewModel.syncGeofence(
                            isGranted = isGranted,
                            isMapLoaded = isMapLoaded
                        )
                    }
                }

                MapsScreen(
                    cameraUpdateEvent = cameraUpdateEvent,
                    mapsUiState = mapsUiState,
                    mapsActions = mapsActions
                )
            }
        }
    }
}