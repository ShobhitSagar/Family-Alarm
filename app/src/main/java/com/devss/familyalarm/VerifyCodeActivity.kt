package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_code.*

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        title = "Enter your Code"

        auth = FirebaseAuth.getInstance()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()

    }

    fun verifyCode(view: View) {

        val otp = code_et.text.toString().trim()
        if (otp.isNotEmpty()) {
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
            signInWithPhoneAuthCredential(credential)
        } else Toast.makeText(applicationContext, "Enter OTP", Toast.LENGTH_SHORT).show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this, "Invalid OTP.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}