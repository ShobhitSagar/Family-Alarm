package com.devss.familyalarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devss.familyalarm.App.Companion.REPLY_CHANNEL_ID
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
    private var senderName = ""
    private var pressedTime = 0L
    private val tempContacts = arrayOf("Shobhit", "Mahavir", "Vipul", "Shubham", "Rinku", "Sudo", "Sagar")

    private lateinit var curUserId: String
    private lateinit var userAllContacts: ArrayList<String>
    private lateinit var serviceIntent: Intent

    private lateinit var auth: FirebaseAuth
    private lateinit var rootUserDbRef: DatabaseReference
    private lateinit var alertDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyCurrentUser()
        checkBatteryOptimizations()

        serviceIntent = Intent(this, MyService::class.java)
        // Stop Service
        startService(serviceIntent)

        val database = Firebase.database
        rootUserDbRef = database.getReference("users/")
        alertDbRef = database.getReference("users/$curUserId/alert/")
        initialiseUserData()

        rootUserDbRef.child(curUserId).child("name").get().addOnSuccessListener {
            val userName = it.value.toString()
            title = userName
        }.addOnFailureListener {
            title = applicationInfo.loadLabel(packageManager).toString()
        }

        call_cb.setOnCheckedChangeListener { _, isChecked -> reqFlag = isChecked }
        location_cb.setOnCheckedChangeListener { _, isChecked -> reqFlag = isChecked }

//        listenReceiver()

        message_et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val id = id_et.text.toString()
                receiverId = if (id.isNotBlank()) id else "1"
                sendAlert()
                return@setOnEditorActionListener true
            }
            false
        }

        send_btn.setOnClickListener {
            val id = id_et.text.toString()
            receiverId = if (id.isNotBlank()) id else "+919560258881"
            sendAlert()
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
        val msg = message_et.text.toString()
        val opt1 = opt1_et.text.toString()
        val opt2 = opt2_et.text.toString()
        if (reqFlag or msg.isNotEmpty()) {
            rootUserDbRef.child(receiverId).child("message").setValue(msg)
            rootUserDbRef.child(receiverId).child("sender").setValue(curUserId)
            rootUserDbRef.child(receiverId).child("sendername").setValue(title)
            rootUserDbRef.child(receiverId).child("reply").setValue("")
            rootUserDbRef.child(receiverId).child("opt1").setValue(if (opt1.isNotEmpty()) opt1 else "YES")
            rootUserDbRef.child(receiverId).child("opt2").setValue(if (opt2.isNotEmpty()) opt2 else "NO")
            rootUserDbRef.child(receiverId).child("call").setValue(if (call_cb.isChecked) "1" else "0")
            rootUserDbRef.child(receiverId).child("location").setValue(if (location_cb.isChecked) "1" else "0")
            rootUserDbRef.child(receiverId).child("alert").setValue("1")

            toastS("Message Sent!")
            listenReceiver()
        } else
            toastS("Please! Enter a message.")
    }

    private fun listenReceiver() {

        rootUserDbRef.child(receiverId).child("received").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val received = snapshot.value
                if (received == "1") {
                    toastS("Alert Delivered!")
//                    rootUserDbRef.child(receiverId).child("received").setValue("0")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastS(error.message)
            }
        })
    }

    private fun initialiseUserData() {
        var alert = ""
        rootUserDbRef.child(curUserId).child("alert").get().addOnSuccessListener { alert = it.value.toString() }
        if (alert == "0") {
            rootUserDbRef.child(curUserId).child("message").setValue("")
            rootUserDbRef.child(curUserId).child("opt1").setValue("YES")
            rootUserDbRef.child(curUserId).child("opt2").setValue("NO")
            rootUserDbRef.child(curUserId).child("received").setValue("0")
            rootUserDbRef.child(curUserId).child("reply").setValue("")
            rootUserDbRef.child(curUserId).child("sender").setValue("")
            rootUserDbRef.child(curUserId).child("sendername").setValue("")

            rootUserDbRef.child(curUserId).child("call").setValue("0")
            rootUserDbRef.child(curUserId).child("location").setValue("0")
        }
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
//                builder.setNegativeButton("Cancel") { _, _ ->
//
//                }

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

    private fun userContacts(): ArrayList<String> {
        var nameList: ArrayList<String> = ArrayList<String>()
        val cr: ContentResolver = contentResolver
        var cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if ((if (cur != null) cur.getCount() else 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                cur.getColumnIndex(ContactsContract.Contacts._ID)
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                nameList.add(name)
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    var pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf<String?>(id),
                        null
                    )
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            val phoneNo =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                        pCur.close()
                    }
                }
            }
        }

        if (cur != null) {
            cur.close()
        }

        return nameList
    }

    private fun loadUserContacts() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS ) == PackageManager.PERMISSION_GRANTED) {
//            userAllContacts = userContacts()

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tempContacts)
            id_actv.setAdapter(adapter)
//            Log.d(TAG, "onCreate: $userAllContacts")
            Log.d(TAG, "onCreate: $tempContacts")
        } else {
            toastS("Contact permission is not granted!")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 9)
        }
    }

    private fun verifyCurrentUser() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            curUserId = currentUser.phoneNumber.toString()
            loadUserContacts()
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    open fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}