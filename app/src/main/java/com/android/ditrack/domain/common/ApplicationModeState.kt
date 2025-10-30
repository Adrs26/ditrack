package com.android.ditrack.domain.common

sealed class ApplicationModeState {
    data object Idle : ApplicationModeState()
    data object Wait : ApplicationModeState()
    data object Drive : ApplicationModeState()
    data object Arrive : ApplicationModeState()
}