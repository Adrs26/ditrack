package com.android.ditrack

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DitrackApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}