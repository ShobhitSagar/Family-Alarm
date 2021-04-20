package com.devss.familyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ReplyNotification : Application() {

    companion object {
        val REPLY_CHANNEL_ID = "replyServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

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

    private fun versionIsAboveOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}