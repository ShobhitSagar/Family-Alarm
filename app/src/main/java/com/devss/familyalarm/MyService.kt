package com.devss.familyalarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.devss.familyalarm.App.Companion.CHANNEL_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyService : Service() {

//    private lateinit var database: DatabaseReference

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = intent?.getStringExtra("inputExtra")
        val curUserId = intent?.getStringExtra("curuserid")

        var database: DatabaseReference = Firebase.database.reference.child("users").child(curUserId.toString())

        displayAlert(database)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification : Notification? = NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("My Service")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun displayAlert(myRef: DatabaseReference) {

        val intent = Intent(this, DisplayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val alertListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("alert").value.toString().equals("1")) {
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        myRef.addValueEventListener(alertListner)
    }
}