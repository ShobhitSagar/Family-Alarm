package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_code.*

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var snackbar: Snackbar
    lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        title = "Enter your Code"

        auth = FirebaseAuth.getInstance()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()

        code_et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCode(v)
                return@setOnEditorActionListener true
            }
            false
        }

    }

    fun verifyCode(view: View) {

        val otp = code_et.text.toString().trim()
        if (otp.isNotEmpty()) {
            snackbar = Snackbar.make(view, "Please wait...", Snackbar.LENGTH_INDEFINITE)
            snackbar.show()
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
            signInWithPhoneAuthCredential(credential)
        } else toastS("Enter OTP")
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    toastS("Invalid OTP.")
                    snackbar.dismiss()
                }
            }
        }

    }

    open fun toastS(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}