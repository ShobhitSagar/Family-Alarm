package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbRef = Firebase.database.reference
    }

    fun save_name_btn(view: View) {
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser.phoneNumber
        
        val userName = name_et.text.toString().trim()
        
        if (userName.isNotBlank()) {

            dbRef.child("users2").child(currentUserId).child("profile").child("name").setValue(userName)
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()

        } else Snackbar.make(view, "Please enter a name.", Snackbar.LENGTH_SHORT).show()

        
    }
}