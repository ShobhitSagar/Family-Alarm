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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("users/1/")

        call_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }
        location_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) reqFlag = true else reqFlag = false
        }

        displayAlert(myRef)

        send_btn.setOnClickListener { sendAlert(myRef) }
    }

    private fun displayAlert(myRef: DatabaseReference) {

        val intent = Intent(this, DisplayActivity::class.java)
        val alertListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                temp_tv.text = snapshot.child("users").toString()
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

    fun sendAlert(myRef: DatabaseReference) {
        val msg = message_et.text.toString()
        val opt1 = opt1_et.text.toString()
        val opt2 = opt2_et.text.toString()
        if (reqFlag or msg.isNotEmpty()) {
            myRef.child("message").setValue(msg)
            myRef.child("alert").setValue("1")
            myRef.child("opt1 ").setValue(if (opt1.isNotEmpty()) opt1 else "YES")
            myRef.child("opt2").setValue(if (opt2.isNotEmpty()) opt2 else "NO")
            myRef.child("call").setValue(if (call_cb.isChecked) "1" else "0")
            myRef.child("location").setValue(if (location_cb.isChecked) "1" else "0")
        } else
            Toast.makeText(applicationContext, "Please! Enter a message.", Toast.LENGTH_SHORT).show()
    }
}