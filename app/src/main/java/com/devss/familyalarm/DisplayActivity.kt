package com.devss.familyalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_display.*

class DisplayActivity : AppCompatActivity() {

    private val TAG = "DisplayActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        val database = Firebase.database
        val myRef = database.getReference("users/1")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val msg = snapshot.child("message").getValue().toString()
                message_tv.text = msg
                Log.d(TAG, "onDataChange: "+msg)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())
            }
        })

        option1_btn.setOnClickListener { disableAlert(myRef) }
        option2_btn.setOnClickListener { disableAlert(myRef) }
        reply_btn.setOnClickListener { disableAlert(myRef) }
        call_btn.setOnClickListener { disableAlert(myRef) }
        location_btn.setOnClickListener { disableAlert(myRef) }
        snooze_btn.setOnClickListener { disableAlert(myRef) }
        cancel_btn.setOnClickListener { disableAlert(myRef) }

    }

    fun disableAlert(myRef: DatabaseReference) {
        myRef.child("alert").setValue("0")
        finish()
    }
}