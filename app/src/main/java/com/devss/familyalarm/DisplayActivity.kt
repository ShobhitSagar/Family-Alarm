package com.devss.familyalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_display.*

class DisplayActivity : AppCompatActivity() {

    private val TAG = "DisplayActivity"
    private lateinit var currentUserId: String
    private var senderId = ""

    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        currentUser()

        val database = Firebase.database
        val myRef = database.getReference("users/")
        val dbRef = database.getReference("users/")

        displayAlert(myRef)

        option1_btn.setOnClickListener { cancelAlert(myRef) }
        option2_btn.setOnClickListener { cancelAlert(myRef) }
        reply_btn.setOnClickListener { 
            val text = reply_et.text.trim().toString()
            if (text.isNotEmpty() or text.isNotBlank()) sendReply(myRef, dbRef, text) else Toast.makeText(
                this,
                "Enter a reply.",
                Toast.LENGTH_SHORT
            ).show()
        }
        call_btn.setOnClickListener {  }
        location_btn.setOnClickListener {  }
        snooze_btn.setOnClickListener { cancelAlert(myRef) }
        cancel_btn.setOnClickListener { cancelAlert(myRef) }

    }

    private fun currentUser() {
        auth = FirebaseAuth.getInstance()

        if (auth.toString().isNotBlank()) currentUserId = auth.currentUser.phoneNumber
    }

    private fun sendReply(myRef: DatabaseReference, dbRef: DatabaseReference, text: String) {
        val senderId = sender_tv.text.toString()
        dbRef.child(senderId).child("reply").setValue(text)
        cancelAlert(myRef)
    }

    private fun displayAlert(myRef: DatabaseReference) {
        myRef.child(currentUserId).child("received").setValue("1")

//        myRef.child(currentUserId).child("sender").get().addOnSuccessListener { senderId = it.value.toString() }
//        myRef.child(senderId).child("name").get().addOnSuccessListener { sender_tv.text = it.value.toString() }
//        myRef.child(currentUserId).child("message").get().addOnSuccessListener { message_tv.text = it.value.toString() }
//        myRef.child(currentUserId).child("opt1").get().addOnSuccessListener { option1_btn.text = it.value.toString() }
//        myRef.child(currentUserId).child("opt2").get().addOnSuccessListener { option2_btn.text = it.value.toString() }


        myRef.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senderId = snapshot.child("sender").value.toString()
                myRef.child(senderId).child("name").get().addOnSuccessListener { sender_tv.text = it.value.toString() }

                val msg = snapshot.child("message").getValue().toString()

                if (msg.isNotEmpty()) {
                    message_tv.text = msg
                    option1_btn.text = snapshot.child("opt1").value.toString()
                    option2_btn.text = snapshot.child("opt2").value.toString()
                } else message_layout.visibility = View.GONE
                if (snapshot.child("call").value.toString() == "1") call_btn.visibility = View.VISIBLE
                if (snapshot.child("location").value.toString() == "1") location_btn.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun cancelAlert(myRef: DatabaseReference) {
        resetUserData(myRef)
        finish()
    }

    private fun resetUserData(myRef: DatabaseReference) {
//        myRef.child(currentUserId).child("received").setValue("0")
        myRef.child(currentUserId).child("opt1").setValue("YES")
        myRef.child(currentUserId).child("opt2").setValue("NO")
        myRef.child(currentUserId).child("call").setValue("0")
        myRef.child(currentUserId).child("location").setValue("0")
        myRef.child(currentUserId).child("alert").setValue("0")
    }
}