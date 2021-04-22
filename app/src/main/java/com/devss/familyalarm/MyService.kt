package com.devss.familyalarm

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devss.familyalarm.App.Companion.MESSAGE_CHANNEL_ID
import com.devss.familyalarm.App.Companion.REPLY_CHANNEL_ID
import com.devss.familyalarm.App.Companion.SERVICE_CHANNEL_ID
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
    private lateinit var alertDbRef: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        authenticateUser()

        dbRef = Firebase.database.reference.child("users").child(currentUserId)
        alertDbRef = Firebase.database.getReference("users/$currentUserId/alert/")

        displayAlert()
        createServiceNotification()

        listenReply()
        return START_STICKY
    }

    private fun listenReply() {

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.child("alert").value.toString()
                val reply = snapshot.child("reply").value.toString()

                if (alert == "2") {
                    senderName = snapshot.child("sendername").value.toString()
//                    Toast.makeText(applicationContext, "Replied!", Toast.LENGTH_SHORT).show()
                    showReplyNotification(reply)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastS(error.message)
            }
        })
    }

    private fun createServiceNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification: Notification? = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentText("Family App is running in background.")
            .setSmallIcon(R.drawable.ic_alarm)
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

        alertDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.value

                if (alert == "1") {
                    var message = ""
                    dbRef.child("sendername").get().addOnSuccessListener {
                        senderName = it.value.toString()
                        dbRef.child("message").get().addOnSuccessListener {
                            message = it.value.toString()
                            showMessageNotification(message)
                        }
                    }
//                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastS(error.message)
            }
        })
    }

    private fun showMessageNotification(message: String) {
        val notificationIntent = Intent(this, DisplayActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        var builder = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(senderName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            // TODO: Viberate
            // .setVibrate()
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(5, builder.build())

        }
    }

    private fun showReplyNotification(reply: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        var builder = NotificationCompat.Builder(this, REPLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reply)
            .setContentTitle(senderName)
            .setContentText(reply)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(4, builder.build())

            dbRef.child("alert").setValue("0")
        }
    }

    fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}