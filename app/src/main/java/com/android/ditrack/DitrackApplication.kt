package com.android.ditrack

import android.app.Application
import com.android.ditrack.data.datastore.UserSessionPreferences
import com.android.ditrack.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DitrackApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DitrackApplication)
            modules(appModule)
        }

        applicationScope.launch {
            UserSessionPreferences(applicationContext).clearData()
        }
    }
}