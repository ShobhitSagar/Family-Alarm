package com.devss.familyalarm

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ContactsAdapter(
    private val context: Activity,
    protected val contactList: ArrayList<Contact>
) : ArrayAdapter<Contact>(context, R.layout.contacts_list, contactList) {

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val inflater = context.layoutInflater
//        val contactList = inflater.inflate(R.layout.contacts_list, null, true)
//
//        val name = convertView?.findViewById<TextView>(R.id.name_tv)
//        val contact = convertView?.findViewById<TextView>(R.id.number_tv)
//
//        val contact: Contact = contactList
//    }

}

