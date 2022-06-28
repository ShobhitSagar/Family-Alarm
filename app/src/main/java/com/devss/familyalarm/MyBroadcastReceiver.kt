package com.devss.familyalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyBroadcastReceiver: BroadcastReceiver() {

    private val TAG = "MyBroadcastReceiver"

    private lateinit var auth: FirebaseAuth
    private lateinit var contactsDbRef: DatabaseReference
    private lateinit var currentUserId: String

    override fun onReceive(context: Context?, intent: Intent?) {

        authenticateUser()

        val database = Firebase.database
        contactsDbRef = database.getReference("users2/$currentUserId/contacts/")

        if (intent?.action.equals("delete_reply")) {
            deleteReply(context)
        }

        if (intent?.action.equals("delete_alert")) {
            deleteAlert(context)
        }
    }

    private fun deleteAlert(context: Context?) {
        contactsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    contactsDbRef.child(it.key.toString()).child("reply").removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun deleteReply(context: Context?) {
        contactsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    contactsDbRef.child(it.key.toString()).child("reply").removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun authenticateUser() {
        auth = FirebaseAuth.getInstance()

        if (auth.toString().isNotBlank()) {
            currentUserId = auth.currentUser.phoneNumber
        }
    }
}