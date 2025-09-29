package com.android.ditrack.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.ditrack.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channelId = "tracking_channel"
    private val channelName = "Tracking Bus Channel"
    private val notificationId = 1

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()
        createTrackingNotificationChannel()
        startForeground(notificationId, buildTrackingNotification(0, "20.15"))
        Log.d("ForegroundLocationService", "onCreate()")

        serviceScope.launch {
            var progress = 1
            while (isActive) {
                withContext(Dispatchers.Main) {
                    updateNotification(progress, "20.15")
                }
                progress++
                delay(1000)
                if (progress > 100) break
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        Log.d("ForegroundLocationService", "onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        Log.d("ForegroundLocationService", "onDestroy()")
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
            .setContentTitle("500 m lagi menuju halte A")
            .setContentText("Perkiraan sampai dalam 5 menit")
            .setSmallIcon(R.drawable.ic_directions_bus)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(progress: Int, time: String) {
        NotificationManagerCompat.from(this).notify(
            notificationId,
            buildTrackingNotification(progress, time)
        )
    }
}