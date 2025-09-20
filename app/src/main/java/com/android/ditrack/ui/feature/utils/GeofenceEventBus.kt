package com.android.ditrack.ui.feature.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GeofenceEventBus {
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    suspend fun postEvent(message: String) {
        _events.emit(message)
    }
}