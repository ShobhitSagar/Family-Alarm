package com.devss.familyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {

    companion object {
        const val SERVICE_CHANNEL_ID = "alarmServiceChannel"
        const val REPLY_CHANNEL_ID = "replyNotificationChannel"
        const val MESSAGE_CHANNEL_ID = "messageNotificationChannel"
        const val MESSAGE_NOTIFICATION_ID = 5
        const val REPLY_NOTIFICATION_ID = 6
    }

    override fun onCreate() {
        super.onCreate()

        createServiceNotificationChannel()
        createReplyNotificationChannel()
        createMessageNotificationChannel()
    }

    // TODO: REPLY NOTIFICATION
    private fun createReplyNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val replyChannel = NotificationChannel(
                REPLY_CHANNEL_ID,
                "Reply",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(replyChannel)
        }
    }

    private fun createMessageNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val replyChannel = NotificationChannel(
                MESSAGE_CHANNEL_ID,
                "Message",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(replyChannel)
        }
    }

    private fun createServiceNotificationChannel() {
        if (versionIsAboveOreo()) run {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "App running in background",
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