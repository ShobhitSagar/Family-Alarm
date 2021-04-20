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
    private var pressedTime = 0L

    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        currentUser()

        val database = Firebase.database
        myRef = database.getReference("users/")

        displayAlert()

        option1_btn.setOnClickListener {
            sendReply(option1_btn.text.toString())
        }
        option2_btn.setOnClickListener {
            sendReply(option2_btn.text.toString())
        }
        reply_btn.setOnClickListener {
            val text = reply_et.text.trim().toString()
            if (text.isNotEmpty() or text.isNotBlank()) sendReply(text) else Toast.makeText(
                this,
                "Enter a reply.",
                Toast.LENGTH_SHORT
            ).show()
        }
        call_btn.setOnClickListener { cancelAlert() }
        location_btn.setOnClickListener { cancelAlert() }
        snooze_btn.setOnClickListener { cancelAlert() }
        cancel_btn.setOnClickListener { cancelAlert() }

    }

    private fun currentUser() {
        auth = FirebaseAuth.getInstance()

        if (auth.toString().isNotBlank()) currentUserId = auth.currentUser.phoneNumber
    }

    private fun sendReply(text: String) {
        myRef.child(senderId).child("alert").setValue("2")
        myRef.child(senderId).child("reply").setValue(text)
        myRef.child(senderId).child("sender").setValue(currentUserId)
        myRef.child(senderId).child("sendername").setValue(currentUserName)
        cancelAlert()
    }

    private fun displayAlert() {
        myRef.child(currentUserId).child("received").setValue("1")

//        myRef.child(currentUserId).child("sender").get().addOnSuccessListener { senderId = it.value.toString() }
//        myRef.child(senderId).child("name").get().addOnSuccessListener { sender_tv.text = it.value.toString() }
//        myRef.child(currentUserId).child("message").get().addOnSuccessListener { message_tv.text = it.value.toString() }
//        myRef.child(currentUserId).child("opt1").get().addOnSuccessListener { option1_btn.text = it.value.toString() }
//        myRef.child(currentUserId).child("opt2").get().addOnSuccessListener { option2_btn.text = it.value.toString() }


        myRef.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senderId = snapshot.child("sender").value.toString()
                currentUserName = snapshot.child("name").value.toString()
                sender_tv.text = currentUserName

                val msg = snapshot.child("message").getValue().toString()

                if (msg.isNotEmpty()) {
                    message_tv.text = msg
                    option1_btn.text = snapshot.child("opt1").value.toString()
                    option2_btn.text = snapshot.child("opt2").value.toString()
                } else message_layout.visibility = View.GONE
                if (snapshot.child("call").value.toString() == "1") call_btn.visibility =
                    View.VISIBLE
                if (snapshot.child("location").value.toString() == "1") location_btn.visibility =
                    View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun cancelAlert() {
        resetUserData()
        finish()
    }

    private fun resetUserData() {
        myRef.child(currentUserId).child("alert").setValue("0")
        myRef.child(currentUserId).child("message").setValue("")
        myRef.child(currentUserId).child("opt1").setValue("YES")
        myRef.child(currentUserId).child("opt2").setValue("NO")

        myRef.child(currentUserId).child("call").setValue("0")
        myRef.child(currentUserId).child("location").setValue("0")
    }

    // TODO: Handle back pressed
    override fun onBackPressed() {
        resetUserData()
        finish()
    }
}