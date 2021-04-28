package com.devss.familyalarm

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devss.familyalarm.App.Companion.MESSAGE_CHANNEL_ID
import com.devss.familyalarm.App.Companion.MESSAGE_NOTIFICATION_ID
import com.devss.familyalarm.App.Companion.REPLY_CHANNEL_ID
import com.devss.familyalarm.App.Companion.REPLY_NOTIFICATION_ID
import com.devss.familyalarm.App.Companion.SERVICE_CHANNEL_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainService : Service() {

    private val TAG = "MainService"
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    //    var msgNotificationId: Int = 5
    private lateinit var currentUserDbRef: DatabaseReference
    private lateinit var alertDbRef: DatabaseReference
    private lateinit var contactsDbRef: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        authenticateUser()

        currentUserDbRef = Firebase.database.reference.child("users2/$currentUserId/")
        alertDbRef = Firebase.database.getReference("users2/$currentUserId/alerts/")
        contactsDbRef = Firebase.database.getReference("users2/$currentUserId/contacts/")

        displayAlert()
        createServiceNotification()

        listenReply()
        return START_STICKY
    }

    private fun listenReply() {

        currentUserDbRef.child("contacts").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach {
                    val reply = it.child("reply").value.toString()
                    if (it.child("reply").exists()) {
                        val senderName = it.child("name").value.toString()
                        showReplyNotification(senderName, reply)
                    }
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
            .setContentText("Running in background.")
            .setSmallIcon(R.drawable.ic_alert)
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

        alertDbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.exists()) {

                    alertDbRef.child(snapshot.key.toString()).child("sender").get()
                        .addOnSuccessListener {
                            contactsDbRef.child(it.value.toString()).child("name").get().addOnSuccessListener {
                                val senderName = it.value.toString()
                                alertDbRef.child(snapshot.key.toString()).child("message").get()
                                    .addOnSuccessListener {
                                        val message = it.value.toString()
                                        showMessageNotification(senderName, message)
                                    }
                            }
                        }
                } else toastS("No Data Found!")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun showMessageNotification(senderName: String, message: String) {
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
        val notificationIntent = Intent(this, DisplayActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        var builder = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(senderName)
            .setContentText(message)
            .setVibrate(pattern)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setLights(Color.BLUE, 500, 500)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setStyle(NotificationCompat.InboxStyle())
            .addAction(R.drawable.ic_reply, "REPLY", pendingIntent)
//        TODO:
//            .addAction(R.drawable.ic_reply, "YES", pendingIntent)
//            .addAction(R.drawable.ic_reply, "NO", pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(MESSAGE_NOTIFICATION_ID, builder.build())

        }
    }

    private fun showReplyNotification(senderName: String, reply: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingNotificationIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val deleteIntent = Intent(this, MyBroadcastReceiver::class.java)
        deleteIntent.setAction("delete_reply")
        val deletePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        var builder = NotificationCompat.Builder(this, REPLY_CHANNEL_ID)
            .setContentTitle(senderName)
            .setSmallIcon(R.drawable.ic_reply)
            .setContentText(reply)
//            .setContentIntent(pendingNotificationIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDeleteIntent(deletePendingIntent)

        with(NotificationManagerCompat.from(this)) {
            notify(REPLY_NOTIFICATION_ID, builder.build())
        }
    }

    fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}