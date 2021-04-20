package com.devss.familyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {

    companion object {
        val SERVICE_CHANNEL_ID = "alarmServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

        createServiceNotificationChannel()
    }

    private fun createServiceNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Example Service Channel",
                NotificationManager.IMPORTANCE_HIGH,
            )
            serviceChannel.setShowBadge(false)
            serviceChannel.setSound(null, null)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun versionIsAboveOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}