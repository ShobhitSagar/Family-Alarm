package com.devss.familyalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private lateinit var auth: FirebaseAuth
    private lateinit var snackbar: Snackbar
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title = "Enter your phone number"

        auth = FirebaseAuth.getInstance()

        handleCallBacks()

        number_et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                AuthenticateUser(v)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun handleCallBacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                snackbar.dismiss()
                Toast.makeText(applicationContext, "${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId
                resendToken = token
                var intent = Intent(applicationContext, VerifyCodeActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
            }

        }
    }

    override fun onStart() {
        super.onStart()

        var currentUser = auth.currentUser
        if (currentUser != null) {
            // TODO: Change MainActivity <-> ProfileActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun AuthenticateUser(view: View) {
        val areaCode = area_code_et.text.toString().trim()
        phoneNumber = number_et.text.toString().trim()

        // TODO: This Intent must be called for Code verification
//        intent = Intent(this, VerifyCodeActivity::class.java)
//        startActivity(intent)

        sendVerificationCode(view, areaCode, phoneNumber)
    }

    private fun sendVerificationCode(view: View, areaCode: String, phoneNumber: String) {

        if (areaCode.isNotEmpty() && phoneNumber.length == 10) {
            val phnNum = "+$areaCode$phoneNumber"
            snackbar = Snackbar.make(view, "Please wait...", Snackbar.LENGTH_INDEFINITE)
            snackbar.show()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phnNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else Snackbar.make(view, "Please enter a valid number.", Snackbar.LENGTH_SHORT).show()
    }
}