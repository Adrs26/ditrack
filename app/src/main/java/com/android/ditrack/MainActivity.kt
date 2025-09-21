package com.android.ditrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.ditrack.ui.feature.screen.MapsScreen
import com.android.ditrack.ui.feature.screen.MapsViewModel
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
                val busStopsDummy = listOf(
                    LatLng(-7.050572, 110.439513),
                    LatLng(-7.052850, 110.442370),
                    LatLng(-7.054480, 110.444680),
                    LatLng(-7.056800, 110.448900),
                    LatLng(-7.058420, 110.452300),
                    LatLng(-7.060100, 110.456200),
                    LatLng(-7.063000, 110.460500),
                    LatLng(-7.066500, 110.463800),
                    LatLng(-7.057581, 110.440196)
                )

                val viewModel = koinViewModel<MapsViewModel>()
                val geofenceTransition by viewModel.geofenceTransition.collectAsStateWithLifecycle()
                val busStopId by viewModel.busStopId.collectAsStateWithLifecycle()

                MapsScreen(
                    busStops = busStopsDummy,
                    geofenceTransition = geofenceTransition,
                    busStopId = busStopId,
                    onResetGeofenceTransition = viewModel::resetGeofenceTransition
                )
            }
        }
    }
}