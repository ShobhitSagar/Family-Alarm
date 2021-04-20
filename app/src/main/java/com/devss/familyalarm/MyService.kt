package com.devss.familyalarm

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devss.familyalarm.App.Companion.SERVICE_CHANNEL_ID
import com.devss.familyalarm.ReplyNotification.Companion.REPLY_CHANNEL_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyService : Service() {

    private val TAG = "MyService"
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private var senderName: String = ""
    private lateinit var dbRef: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        authenticateUser()
        dbRef = Firebase.database.reference.child("users").child(currentUserId)

        displayAlert()
        createNotification()

        showNotification()
        return START_STICKY
    }

    private fun showNotification() {

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.child("alert").value.toString()
                val reply = snapshot.child("reply").value.toString()

                if (alert == "2") {
                    senderName = snapshot.child("sendername").value.toString()
                    Toast.makeText(applicationContext, "Replied!", Toast.LENGTH_SHORT).show()
                    showReplyNotification(reply)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification: Notification? = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentText("Family App is running in background.")
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .build()
        startForeground(1, notification)
    }

    private fun authenticateUser() {
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser.phoneNumber
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun displayAlert() {

        val intent = Intent(this, DisplayActivity::class.java)

        // TODO: WHY THIS FLAG
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        dbRef.child("alert").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.value

                if (alert == "1") {
                    createNotification()
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun showReplyNotification(reply: String) {

        var builder = NotificationCompat.Builder(this, REPLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile)
            .setContentTitle(senderName)
            .setContentText(reply)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "getString(R.string.channel_name)"
                val descriptionText = "getString(R.string.channel_description)"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(REPLY_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        with(NotificationManagerCompat.from(this)) {
            notify(555, builder.build())
        }
    }
}