package com.devss.familyalarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private var reqFlag = false
    var receiverId = ""

    private var pressedTime = 0L
    lateinit var hashMap: LinkedHashMap<String, String>

    private lateinit var currentUserID: String
    private lateinit var serviceIntent: Intent

    private lateinit var auth: FirebaseAuth
    private lateinit var rootUserDbRef: DatabaseReference
    private lateinit var receiverAlertsDbRef: DatabaseReference
    private lateinit var alertDbRef: DatabaseReference
    private lateinit var contactDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyCurrentUser()
        cancelNotifications()
        checkBatteryOptimizations()

        serviceIntent = Intent(this, MainService::class.java)
        // Stop Service
        startService(serviceIntent)

        loadContacts()

        rootUserDbRef.child(currentUserID).child("profile").child("name").get()
            .addOnSuccessListener {
                val userName = it.value.toString()
                title = if (userName.isNotBlank()) userName else applicationInfo.loadLabel(
                    packageManager
                ).toString()
            }.addOnFailureListener {
            title = applicationInfo.loadLabel(packageManager).toString()
        }

        call_cb.setOnCheckedChangeListener { _, isChecked -> reqFlag = isChecked }
        location_cb.setOnCheckedChangeListener { _, isChecked -> reqFlag = isChecked }

//        listenReceiver()

        message_et.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val id = id_actv.text.toString()
                if (id.isNotBlank()) {
                    receiverId = hashMap?.getValue(id)
                    sendAlert()
                } else Snackbar.make(v, "Select a contact", Snackbar.LENGTH_SHORT).show()
                return@setOnEditorActionListener true
            }
            false
        }

        send_btn.setOnClickListener {
            val id = id_actv.text.toString()
            if (id.isNotBlank()) {
                receiverId = hashMap.getValue(id)
                sendAlert()
            } else Snackbar.make(root_layout, "Select a contact", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        hashMap = LinkedHashMap<String, String>()

        val snackbar: Snackbar =
            Snackbar.make(root_layout, "Please wait...", Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
        val contactAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
//        contactDbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                snapshot.children.forEach {
//                    val name = it.child("name").value.toString()
//                    val number = it.key.toString()
//                    hashMap.put(name, number)
//                    contactAdapter.add(name)
//                }
//                snackbar.dismiss()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })

        contactDbRef.get().addOnSuccessListener {
            it.children.forEach {
                val number = it.key.toString()
                val name = it.child("name").value.toString()
                hashMap.put(name, number)
                contactAdapter.add(name)

            }
            snackbar.dismiss()
        }

//        contactDbRef.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })

        id_actv.setAdapter(contactAdapter)
    }

    private fun cancelNotifications() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.cancelAll()
        } else {
            TODO("VERSION.SDK_INT < M")
        }

    }

    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun sendAlert() {
        receiverAlertsDbRef = rootUserDbRef.child(receiverId).child("alerts")

        val message = message_et.text.toString()
        val opt1 = opt1_et.text.toString()
        val opt2 = opt2_et.text.toString()
        if (reqFlag or message.isNotBlank()) {
            receiverAlertsDbRef.child(currentUserID).child("message").setValue(message)
            receiverAlertsDbRef.child(currentUserID).child("sender").setValue(currentUserID)
            receiverAlertsDbRef.child(currentUserID).child("sendername").setValue(title)
            receiverAlertsDbRef.child(currentUserID).child("opt1")
                .setValue(if (opt1.isNotBlank()) opt1 else "YES")
            receiverAlertsDbRef.child(currentUserID).child("opt2")
                .setValue(if (opt2.isNotBlank()) opt2 else "NO")
            receiverAlertsDbRef.child(currentUserID).child("call")
                .setValue(if (call_cb.isChecked) "1" else "0")
            receiverAlertsDbRef.child(currentUserID).child("location")
                .setValue(if (location_cb.isChecked) "1" else "0")
//            receiverAlertsDbRef.child(currentUserID).child("alert").setValue("1")

            toastS("Message Sent!")
            initialiseUI()
            listenReceiver()
        } else
            toastS("Please! Enter a message.")
    }

    private fun listenReceiver() {

        contactDbRef.child(receiverId).child("delivered").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    toastS("Alert delivered")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastS(error.message)
            }
        })
    }

    private fun initialiseUI() {
        message_et.setText("")
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
                    toastS("Logged out successfully!")
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
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

    private fun verifyCurrentUser() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            currentUserID = currentUser.phoneNumber.toString()
            rootUserDbRef = Firebase.database.getReference("users2/")
            alertDbRef = Firebase.database.getReference("users2/$currentUserID/alert/")
            contactDbRef = Firebase.database.getReference("users2/$currentUserID/contacts/")
//            loadUserContacts()
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    fun contacts(view: View) {
        startActivity(Intent(this, ContactActivity::class.java))
    }
}