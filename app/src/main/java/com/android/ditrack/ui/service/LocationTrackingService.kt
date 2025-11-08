package com.android.ditrack.ui.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.ditrack.R
import com.android.ditrack.domain.common.ApplicationModeState
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.ui.feature.utils.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationTrackingService : Service(), KoinComponent {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentJob: Job? = null
    private val channelId = "tracking_channel"
    private val channelName = "Tracking Bus Channel"
    private val notificationId = 1

    private val mapsRepository by inject<MapsRepository>()

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationTrackingService", "Service new created")
        createTrackingNotificationChannel()
        startForeground(notificationId, buildTrackingNotification(0, "20.15"))
        mapsRepository.setServiceRunning(true)

        serviceScope.launch {
            mapsRepository.command.collect { command ->
                currentJob?.cancel()
                when (command) {
                    ApplicationModeState.Idle, ApplicationModeState.Arrive -> Unit
                    ApplicationModeState.Wait, ApplicationModeState.Drive  -> {
                        currentJob = serviceScope.launch { startTrackingBus(command) }
                    }
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        currentJob?.cancel()
        serviceScope.cancel()
        Log.d("LocationTrackingService", "Service destroyed")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        mapsRepository.setServiceRunning(false)
        super.onDestroy()
    }

    private fun createTrackingNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildTrackingNotification(progress: Int, time: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("500 m lagi menuju halte tujuan")
            .setContentText("Perkiraan sampai dalam 5 menit")
            .setSmallIcon(R.drawable.ic_directions_bus)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(progress: Int, time: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            notificationId,
            buildTrackingNotification(progress, time)
        )
    }

    private suspend fun startTrackingBus(command: ApplicationModeState) {
        var progress = 1
        while (true) {
            withContext(Dispatchers.Main) {
                updateNotification(progress, "20.15")
            }
            progress++
            delay(1000)
            Log.d("LocationTrackingService", "startTrackingBus: $progress")
            if (progress > 20) {
                when (command) {
                    ApplicationModeState.Idle, ApplicationModeState.Arrive -> Unit
                    ApplicationModeState.Wait -> {
                        NotificationUtil.sendNotification(
                            this@LocationTrackingService,
                            "Bus telah tiba",
                            "Anda akan otomatis beralih ke mode naik bus"
                        )
                        mapsRepository.sendEventFromService(ApplicationModeState.Drive)
                        break
                    }
                    ApplicationModeState.Drive -> {
                        NotificationUtil.sendNotification(
                            this@LocationTrackingService,
                            "Bus telah tiba",
                            "Perjalanan akan otomatis diselesaikan"
                        )
                        mapsRepository.sendEventFromService(ApplicationModeState.Arrive)
                        break
                    }
                }
            }
        }
    }
}