package com.devss.familyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
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
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttr = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM).build()
        val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
        if (versionIsAboveOreo()) run {
            val messageChannel = NotificationChannel(
                MESSAGE_CHANNEL_ID,
                "Message",
                NotificationManager.IMPORTANCE_HIGH
            )
            messageChannel.vibrationPattern = pattern
            messageChannel.setSound(alarmSound, audioAttr)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(messageChannel)
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