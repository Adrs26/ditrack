package com.android.ditrack.domain.model

sealed class ApplicationMode {
    object IDLING : ApplicationMode()
    object WAITING : ApplicationMode()
    object DRIVING : ApplicationMode()
    object ARRIVING : ApplicationMode()
}