package com.devss.familyalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
        val myRef = database.getReference("users/1/")

        displayAlert(myRef)

        option1_btn.setOnClickListener { disableAlert(myRef) }
        option2_btn.setOnClickListener { disableAlert(myRef) }
        reply_btn.setOnClickListener { disableAlert(myRef) }
        call_btn.setOnClickListener { disableAlert(myRef) }
        location_btn.setOnClickListener { disableAlert(myRef) }
        snooze_btn.setOnClickListener { disableAlert(myRef) }
        cancel_btn.setOnClickListener { disableAlert(myRef) }

    }

    private fun displayAlert(myRef: DatabaseReference) {
        myRef.child("received").setValue("1")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sender_tv.text = snapshot.child("sender").value.toString()
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

    fun disableAlert(myRef: DatabaseReference) {
        myRef.child("alert").setValue("0")
        myRef.child("received").setValue("0")
        finish()
    }
}