package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var reqFlag = false
    private val TAG = "MainActivity"
    private val curUserId = "1"
    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("users/")

        myRef.child(curUserId).child("name").get().addOnSuccessListener {
            val appLabel = it.value.toString()
            title = if ((appLabel == "null") or (appLabel == "")) appLabel() else appLabel
        }.addOnFailureListener {
            title = appLabel()
        }

        call_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }
        location_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }

        startNotificationService("Family Alarm is running in background.")

        send_btn.setOnClickListener {
            userId = id_et.text.toString()
            sendAlert(myRef)
        }
    }

    private fun listenReceiver(myRef: DatabaseReference) {
        val postListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("received").value.toString() == "1") {
                    Toast.makeText(applicationContext, "Message Delivered!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        myRef.child(userId).addValueEventListener(postListner)
    }

    private fun appLabel(): String {
        return applicationInfo.loadLabel(packageManager).toString()
    }

    fun sendAlert(myRef: DatabaseReference) {
        val msg = message_et.text.toString()
        val opt1 = opt1_et.text.toString()
        val opt2 = opt2_et.text.toString()
        if (reqFlag or msg.isNotEmpty()) {
            myRef.child(userId).child("message").setValue(msg)
            myRef.child(userId).child("sender").setValue(curUserId)
            myRef.child(userId).child("alert").setValue("1")
            myRef.child(userId).child("opt1").setValue(if (opt1.isNotEmpty()) opt1 else "YES")
            myRef.child(userId).child("opt2").setValue(if (opt2.isNotEmpty()) opt2 else "NO")
            myRef.child(userId).child("call").setValue(if (call_cb.isChecked) "1" else "0")
            myRef.child(userId).child("location").setValue(if (location_cb.isChecked) "1" else "0")

            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show()
            listenReceiver(myRef)
        } else
            Toast.makeText(this, "Please! Enter a message.", Toast.LENGTH_SHORT).show()
    }

    private fun startNotificationService(msg: String) {
        val serviceIntent = Intent(this, MyService::class.java)
        serviceIntent.putExtra("inputExtra", msg)
        serviceIntent.putExtra("curuserid", curUserId)
        startService(serviceIntent)
    }
}