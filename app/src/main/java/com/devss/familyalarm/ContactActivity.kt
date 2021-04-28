package com.devss.familyalarm

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.add_contact.*

class ContactActivity : AppCompatActivity() {

    private val TAG = "ContactActivity"

    private lateinit var auth: FirebaseAuth
    private lateinit var contactsDbRef: DatabaseReference
    private lateinit var dbRef: DatabaseReference

    private lateinit var currentUserId: String
    private var nameList: ArrayList<String> = ArrayList()
    private lateinit var contactAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        title = "Contacts"
        authenticateUser()

        dbRef = Firebase.database.getReference("users2/")
        contactsDbRef = Firebase.database.getReference("users2/$currentUserId/contacts/")

        contactAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameList)
        loadContacts()

//        number_et.setOnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                addContact(v)
//                return@setOnEditorActionListener true
//            }
//                false
//        }

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
        }
    }

    private fun loadContacts() {

        contactsDbRef.get().addOnSuccessListener {
            it.children.forEach {
                val name = it.child("name").value.toString()
                val number = it.key.toString()
                nameList.add("$name\n$number")
            }
            contacts_lv.adapter = contactAdapter
        }
    }

    fun addContact(name: String, number: String) {
//        name = name_et.text.toString()
//        number = number_et.text.toString()

        if (name.isNotBlank()) {
            if (number.isNotBlank()) {
                if (number.length == 13) {
                    dbRef.get().addOnSuccessListener {
                        it.children.forEach {
                            Log.d(TAG, "addContact: $name | $number")
                            if (it.key.toString() == number) {
                                Log.d(TAG, "addContact: $name | $number")
                                contactsDbRef.child(number).child("name").setValue(name)
                                    .addOnSuccessListener {
                                        recreate()
                                        Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show()
                                        Snackbar.make(root_layout, "Contact Added", Snackbar.LENGTH_SHORT).show()
                                    }
                            } else Snackbar.make(root_layout, "The number is not registered!", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } else Snackbar.make(root_layout, "Enter full number.", Snackbar.LENGTH_SHORT).show()
            } else Snackbar.make(root_layout, "Enter a number", Snackbar.LENGTH_SHORT).show()
        } else Snackbar.make(root_layout, "Enter a name", Snackbar.LENGTH_SHORT).show()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        
        when (itemId) {
            R.id.add_contact -> showAlertDialog()
        }
        
        return super.onOptionsItemSelected(item)
    }

    fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Add Contact")

        val alertLayout = layoutInflater.inflate(R.layout.add_contact, null)
        builder.setView(alertLayout)
        
        builder.setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which ->
            val nameET = alertLayout.findViewById<EditText>(R.id.add_name_et)
            val numberET = alertLayout.findViewById<EditText>(R.id.add_number_et)

            val name = nameET.text.toString()
            val number = numberET.text.toString()

            addContact(name, "+91$number")
        })

        val alertDialog = builder.create()
        alertDialog.show()
    }
}