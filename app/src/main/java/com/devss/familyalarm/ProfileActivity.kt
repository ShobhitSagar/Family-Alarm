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

            dbRef.child("users").child(currentUserId).child("name").setValue(userName)
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()

//            val databaseListner = object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (user in snapshot.children) {
//                        val oldUserId = user.key
//                        if (currentUserId == oldUserId) {
//                            name_et.text = snapshot.child(currentUserId).child("name").value
//                            Log.d(TAG, "Current User: $oldUserId")
//                        }
//                        else {
//
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//            toastS(error.message)
//                }
//            }
//            dbRef.child("users").addValueEventListener(databaseListner)

        } else Snackbar.make(view, "Please enter a name.", Snackbar.LENGTH_SHORT).show()

        
    }
}