package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title = "Login"

        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        auth = Firebase.auth
    }

    fun AuthenticateUser(view: View) {
        phoneNumber = number_et.text.toString().trim()

        if (phoneNumber.length == 10) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(p0, p1)
                    }
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        TODO("Not yet implemented")
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        TODO("Not yet implemented")
                    }

                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else Snackbar.make(view, "Please enter a valid number.", Snackbar.LENGTH_SHORT).show()
    }
}