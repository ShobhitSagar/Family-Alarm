package com.devss.familyalarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.devss.familyalarm.App.Companion.CHANNEL_ID
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
    private var message = "Family App is running in background."
//    private lateinit var database: DatabaseReference

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        authenticateUser()

//        var senderDB: DatabaseReference = Firebase.database.reference.child("users").child(senderID.toString())
        var receiverDB: DatabaseReference = Firebase.database.reference.child("users").child(currentUserId)

        displayAlert(receiverDB)
//        replyListner(senderDB)

        createNotification()

        return START_STICKY
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification : Notification? = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    private fun authenticateUser() {
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser.phoneNumber
    }

    private fun replyListner(senderDB: DatabaseReference) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun displayAlert(receiverDB: DatabaseReference) {

        val intent = Intent(this, DisplayActivity::class.java)

        // TODO: WHY THIS FLAG
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        receiverDB.child("alert").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.value
                Log.d(TAG, "onDataChange: $alert")
                if (alert == "1") {
                    Log.d(TAG, "onDataChange: $alert")

                    receiverDB.child("message").get().addOnSuccessListener { message = it.value.toString() }
                    createNotification()
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


//        val alertListner = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.child("alert").value.toString().equals("1")) {
//                    message = snapshot.child("message").value.toString()
//                    createNotification()
//                    startActivity(intent)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        }
//        receiverDB.addValueEventListener(alertListner)
    }
}