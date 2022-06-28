package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    
    private lateinit var currentUserId: String

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        authenticateUser()
        dbRef = Firebase.database.reference

        val snackbar = Snackbar.make(profile_layout, "Please wait...", Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
        dbRef.child("users2").child(currentUserId).child("profile")
            .child("name").get().addOnSuccessListener {
                val name = it.value.toString()
                if (name.isNotBlank() || name != "null") {
                    name_et.setText(it.value.toString())
                    snackbar.dismiss()
                } else snackbar.dismiss()
            }
    }

    private fun authenticateUser() {
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser.phoneNumber
    }

    fun save_name_btn(view: View) {

        val userName = name_et.text.toString().trim()
        
        if (userName.isNotBlank()) {

            dbRef.child("users2").child(currentUserId).child("profile").child("name").setValue(userName)
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()

        } else Snackbar.make(view, "Please enter a name.", Snackbar.LENGTH_SHORT).show()
        
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val userName = name_et.text.toString().trim()
        if (userName.isBlank())
            dbRef.child("users2").child(currentUserId).child("profile").child("name").setValue("User")
    }
}