package com.android.ditrack.ui.feature.screen.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ditrack.data.datastore.ApplicationMode
import com.android.ditrack.data.datastore.GeofenceTransition
import com.android.ditrack.data.datastore.UserSessionPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapsViewModel(
    private val userSessionPreferences: UserSessionPreferences
) : ViewModel() {

    val applicationMode = userSessionPreferences.applicationMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ApplicationMode.DEFAULT)

    val geofenceTransition = userSessionPreferences.geofenceTransition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), GeofenceTransition.DEFAULT)

    val busStopId = userSessionPreferences.busStopId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun changeMode(applicationMode: ApplicationMode) {
        viewModelScope.launch {
            userSessionPreferences.setApplicationMode(applicationMode)
        }
    }
}