package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    private lateinit var databaseRef: DatabaseReference

    var alert = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val alertRef = database.getReference("users/1")

//        myRef.setValue("Hello World!")

        databaseRef = Firebase.database.reference

        databaseRef.child("users").child("1").child("message").setValue("Hello World!")

        val intent = Intent(this, DisplayActivity::class.java)

        val alertListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                Toast.makeText(applicationContext, snapshot.getValue().toString(), Toast.LENGTH_LONG).show()
                temp_tv.text = snapshot.child("alert").value.toString()
                if (snapshot.child("alert").value.toString().equals("1")) {
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        alertRef.addValueEventListener(alertListner)
    }
}