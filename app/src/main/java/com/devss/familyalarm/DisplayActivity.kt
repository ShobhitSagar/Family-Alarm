package com.devss.familyalarm

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_display.*
import kotlinx.android.synthetic.main.activity_main.*

class DisplayActivity : AppCompatActivity() {

    private val TAG = "DisplayActivity"
    private lateinit var currentUserId: String
    private var currentUserName = ""
    private var senderId = ""
    private var senderName = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var rootUsersDbRef: DatabaseReference
    private lateinit var alertsDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        verifyCurrentUser()

        val database = Firebase.database
        rootUsersDbRef = database.getReference("users2/")
        alertsDbRef = database.getReference("users2/$currentUserId/alerts/")

        // Display Alert
        alertsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) displayAlert() else finish()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        // Button Click Listeners
        option1_btn.setOnClickListener {
            sendReply(option1_btn.text.toString())
        }
        option2_btn.setOnClickListener {
            sendReply(option2_btn.text.toString())
        }
        reply_btn.setOnClickListener {
            val text = reply_et.text.trim().toString()
            if (text.isNotEmpty() or text.isNotBlank()) sendReply(text) else toastS("Enter a reply.")
        }
        call_btn.setOnClickListener {
//            val callIntent = Intent(Intent.ACTION_CALL)
//            callIntent.setData(Uri.parse("tel:$senderId"))
//            startActivity(callIntent)
            cancelAlert()
        }
        location_btn.setOnClickListener { cancelAlert() }
        snooze_btn.setOnClickListener { cancelAlert() }
        cancel_btn.setOnClickListener { cancelAlert() }

    }

    private fun displayAlert() {
        alertsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    senderId = it.child("sender").value.toString()

                    rootUsersDbRef.child(currentUserId).child("contacts").child(senderId)
                        .child("name").get()
                        .addOnSuccessListener {
                            senderName = it.value.toString()
                            title = if (senderName.isNotBlank() && senderName != "null") senderName else "New Alert"
                        }

                    if (senderId.isNotBlank() && senderId != "null") alertDelivered()

                    val msg = it.child("message").value.toString()
                    if (msg.isNotEmpty()) {
                        message_tv.text = msg
                        option1_btn.text = it.child("opt1").value.toString()
                        option2_btn.text = it.child("opt2").value.toString()
                    } else message_layout.visibility = View.GONE
                    if (it.child("call").value.toString() == "1") {
                        request_ll.visibility = View.VISIBLE
                        call_btn.visibility = View.VISIBLE
                    }
                    if (it.child("location").value.toString() == "1") {
                        request_ll.visibility = View.VISIBLE
                        location_btn.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        handleAlertDelivery()
    }

    private fun sendReply(text: String) {
        rootUsersDbRef.child(senderId).child("contacts").child(currentUserId).child("reply")
            .setValue(text)
        cancelAlert()
//        rootUsersDbRef.child(senderId).child("replies").child(currentUserId).child("name").setValue(senderName)
    }

    private fun alertDelivered() {
        rootUsersDbRef.child(senderId).child("contacts").child(currentUserId)
            .child("delivered")
            .setValue("1")
    }

    private fun verifyCurrentUser() {
        auth = FirebaseAuth.getInstance()

        if (auth.toString().isNotBlank()) currentUserId = auth.currentUser.phoneNumber
    }

    private fun cancelAlert() {
        rootUsersDbRef.child(senderId).child("contacts").child(currentUserId)
            .child("delivered")
            .removeValue()
        alertsDbRef.child(senderId).removeValue()
//        finishAffinity()
        finish()
    }

    private fun handleAlertDelivery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.cancel(App.MESSAGE_NOTIFICATION_ID)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }

    // TODO: Handle back pressed
    override fun onBackPressed() {
        finish()
        alertsDbRef.child(senderId).removeValue()
//        resetUserData()
    }

    fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}