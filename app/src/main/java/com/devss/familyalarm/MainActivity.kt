package com.devss.familyalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devss.familyalarm.ReplyNotification.Companion.REPLY_CHANNEL_ID
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var reqFlag = false
    private lateinit var curUserId: String
    var receiverId = ""
    private var senderName = ""
    private var pressedTime = 0L
    private var alertFlag = false

    private lateinit var serviceIntent: Intent

    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference
    private lateinit var alertDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyCurrentUser()
        serviceIntent = Intent(this, MyService::class.java)
        // Stop Service
        stopService(serviceIntent)

        val database = Firebase.database
        myRef = database.getReference("users/")
        alertDbRef = database.getReference("users/$curUserId/alert")
        initialiseUserData()

        myRef.child(curUserId).child("name").get().addOnSuccessListener {
            val userName = it.value.toString()
            title = userName
        }.addOnFailureListener {
            title = appLabel()
        }

        call_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }
        location_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }

        displayAlert()

        send_btn.setOnClickListener {
            val id = id_et.text.toString()
            receiverId = if (id.isNotBlank()) id else "1"
            sendAlert()
        }
    }

    private fun verifyCurrentUser() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            curUserId = currentUser.phoneNumber.toString()
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun listenReceiver() {

        myRef.child(receiverId).child("received").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val received = snapshot.value
                if (received == "1") {
                    Toast.makeText(applicationContext, "Message Delivered!", Toast.LENGTH_SHORT)
                        .show()
                    myRef.child(receiverId).child("received").setValue("0")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        myRef.child(curUserId).child("reply").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                temp_tv.text = snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun appLabel(): String {
        return applicationInfo.loadLabel(packageManager).toString()
    }

    fun sendAlert() {
        val msg = message_et.text.toString()
        val opt1 = opt1_et.text.toString()
        val opt2 = opt2_et.text.toString()
        if (reqFlag or msg.isNotEmpty()) {
            myRef.child(receiverId).child("message").setValue(msg)
            myRef.child(receiverId).child("sender").setValue(curUserId)
            myRef.child(receiverId).child("alert").setValue("1")
            myRef.child(receiverId).child("reply").setValue("")
            myRef.child(receiverId).child("opt1").setValue(if (opt1.isNotEmpty()) opt1 else "YES")
            myRef.child(receiverId).child("opt2").setValue(if (opt2.isNotEmpty()) opt2 else "NO")
            myRef.child(receiverId).child("call").setValue(if (call_cb.isChecked) "1" else "0")
            myRef.child(receiverId).child("location").setValue(if (location_cb.isChecked) "1" else "0")

            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show()
            listenReceiver()
        } else
            Toast.makeText(this, "Please! Enter a message.", Toast.LENGTH_SHORT).show()
    }

    private fun displayAlert() {
        alertDbRef.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alert = snapshot.value.toString()
                if (alert == "1") {
                    startActivity(Intent(applicationContext, DisplayActivity::class.java))
                }
                if (alert == "2") {
                    var reply = ""
                    myRef.child(curUserId).child("sendername").get().addOnSuccessListener {
                        senderName = it.value.toString()
                        myRef.child(curUserId).child("reply").get().addOnSuccessListener {
                            reply = it.value.toString()
                            showReplyNotification(reply)
                        }
                    }
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

    private fun initialiseUserData() {
        myRef.child(curUserId).child("alert").setValue("0")
        myRef.child(curUserId).child("message").setValue("")
        myRef.child(curUserId).child("opt1").setValue("YES")
        myRef.child(curUserId).child("opt2").setValue("NO")
        myRef.child(curUserId).child("received").setValue("0")
        myRef.child(curUserId).child("reply").setValue("")
        myRef.child(curUserId).child("sender").setValue("")
        myRef.child(curUserId).child("sendername").setValue("")

        myRef.child(curUserId).child("call").setValue("0")
        myRef.child(curUserId).child("location").setValue("0")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val itemId = item.itemId
        val builder = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")

        when (itemId) {
            R.id.logout -> {

                builder.setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
                builder.setNegativeButton("Cancel") { _, _ ->

                }

                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }
            R.id.profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // TODO: Handle back pressed
    override fun onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Snackbar.make(root_layout, "Press back again to exit!", Snackbar.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()

    }

    override fun onStop() {
        super.onStop()
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        startService(serviceIntent)
    }

    open fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}