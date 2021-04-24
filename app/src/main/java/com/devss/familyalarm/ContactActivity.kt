package com.devss.familyalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity() {

    private val TAG = "ContactActivity"

    private lateinit var auth: FirebaseAuth
    private lateinit var contactsDbRef: DatabaseReference

    private lateinit var currentUserId: String
    private var nameList: ArrayList<String> = ArrayList()
    private lateinit var contactAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        title = "Contacts"

        authenticateUser()
        loadContacts()

        number_et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addContact(v)
                return@setOnEditorActionListener true
            }
                false
        }

        contacts_lv.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(
                applicationContext,
                contactAdapter.getItem(position),
                Toast.LENGTH_SHORT
            ).show() }

    }

    private fun authenticateUser() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            currentUserId = currentUser.phoneNumber.toString()
            contactsDbRef = Firebase.database.getReference("users2/$currentUserId/contacts/")
        }
    }

    private fun loadContacts() {

        contactAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameList)
        contactsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val name = it.child("name").value.toString()
                        val number = it.key.toString()
                        nameList.add("$name\n$number")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        contacts_lv.adapter = contactAdapter
    }

    fun addContact(view: View) {
        val name = name_et.text.toString()
        val number = number_et.text.toString()

        if (name.isNotBlank()) {
            if (number.isNotBlank()) {
                if (number.length == 10) {
                    contactsDbRef.child("+91$number").child("name").setValue(name)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Contact added successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else Snackbar.make(root_layout, "Enter full number.", Snackbar.LENGTH_SHORT).show()
            } else Snackbar.make(view, "Enter a number", Snackbar.LENGTH_SHORT).show()
        } else Snackbar.make(view, "Enter a name", Snackbar.LENGTH_SHORT).show()

    }
}