package com.devss.familyalarm

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

        senderId = intent.extras?.get("senderId").toString()

        val database = Firebase.database
        rootUsersDbRef = database.getReference("users2/")
        alertsDbRef = database.getReference("users2/$currentUserId/alerts/")

        rootUsersDbRef.child(currentUserId).child("profile").child("name").get().addOnSuccessListener { senderName = it.value.toString() }

        alertsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) displayAlert() else finish()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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
        call_btn.setOnClickListener { cancelAlert() }
        location_btn.setOnClickListener { cancelAlert() }
        snooze_btn.setOnClickListener { cancelAlert() }
        cancel_btn.setOnClickListener { cancelAlert() }

    }

    private fun sendReply(text: String) {
        rootUsersDbRef.child(senderId).child("contacts").child(currentUserId).child("reply").setValue(text)
        cancelAlert()
//        rootUsersDbRef.child(senderId).child("replies").child(currentUserId).child("name").setValue(senderName)
    }

    private fun displayAlert() {
        alertsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    senderId = it.child("sender").value.toString()
                    senderName = it.child("sendername").value.toString()
                    title = if (senderName.isNotBlank()) senderName else applicationInfo.loadLabel(packageManager).toString()

                    val msg = it.child("message").value.toString()
                    if (msg.isNotEmpty()) {
                        message_tv.text = msg
                        option1_btn.text = it.child("opt1").value.toString()
                        option2_btn.text = it.child("opt2").value.toString()
                    } else message_layout.visibility = View.GONE
                    if (it.child("call").value.toString() == "1") call_btn.visibility =
                        View.VISIBLE
                    if (it.child("location").value.toString() == "1") location_btn.visibility =
                        View.VISIBLE

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun verifyCurrentUser() {
        auth = FirebaseAuth.getInstance()

        if (auth.toString().isNotBlank()) currentUserId = auth.currentUser.phoneNumber
    }

    private fun cancelAlert() {
        finish()
        alertsDbRef.child(senderId).removeValue()
//        resetUserData()
    }

    private fun resetUserData() {
        alertsDbRef.child(senderId).child("alert").setValue("0")
        alertsDbRef.child(senderId).child("message").setValue("")
        alertsDbRef.child(senderId).child("opt1").setValue("YES")
        alertsDbRef.child(senderId).child("opt2").setValue("NO")

        alertsDbRef.child(senderId).child("call").setValue("0")
        alertsDbRef.child(senderId).child("location").setValue("0")
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