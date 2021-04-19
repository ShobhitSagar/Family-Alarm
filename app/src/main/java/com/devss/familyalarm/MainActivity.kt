package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var curUserId = "1"
    var receiverId = ""
    private var pressedTime = 0L

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("users/")

        verifyCurrentUser()

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

        // Service Starts
        startNotificationService("Family Alarm is running in background.")

        send_btn.setOnClickListener {
            val id = id_et.text.toString()
            receiverId = if (id.isNotBlank()) id else "1"
            sendAlert(myRef)
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

    private fun listenReceiver(myRef: DatabaseReference) {

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

//        val postListner = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.child(receiverId).child("received").value.toString() == "1") {
//                    Toast.makeText(applicationContext, "Message Delivered!", Toast.LENGTH_SHORT)
//                        .show()
//                }
//                temp_tv.text = snapshot.child(curUserId).child("reply").value.toString()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        }
//        myRef.addValueEventListener(postListner)
    }

    private fun appLabel(): String {
        return applicationInfo.loadLabel(packageManager).toString()
    }

    fun sendAlert(myRef: DatabaseReference) {
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
            listenReceiver(myRef)
        } else
            Toast.makeText(this, "Please! Enter a message.", Toast.LENGTH_SHORT).show()
    }

    private fun startNotificationService(msg: String) {
        val serviceIntent = Intent(this, MyService::class.java)
//        serviceIntent.putExtra("inputExtra", msg)
//        serviceIntent.putExtra("senderid", curUserId)
//        serviceIntent.putExtra("receiverid", receiverId)
//        Toast.makeText(this, curUserId +" | "+ receiverId, Toast.LENGTH_SHORT).show()
        startService(serviceIntent)
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
}