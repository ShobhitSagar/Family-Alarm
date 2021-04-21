package com.devss.familyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {

    companion object {
        const val SERVICE_CHANNEL_ID = "alarmServiceChannel"
        const val REPLY_CHANNEL_ID = "replyServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

        createServiceNotificationChannel()
        createReplyNotificationChannel()
    }

    // TODO: REPLY NOTIFICATION
    private fun createReplyNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val replyChannel = NotificationChannel(
                REPLY_CHANNEL_ID,
                "Reply Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(replyChannel)
        }
    }

    private fun createServiceNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Example Service Channel",
                NotificationManager.IMPORTANCE_LOW,
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