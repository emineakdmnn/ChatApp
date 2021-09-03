package com.emineakduman.chatapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.emineakduman.chatapp.R
import com.emineakduman.chatapp.util.Constants
import com.emineakduman.chatapp.util.gone
import com.emineakduman.chatapp.util.visible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*



class RegisterActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    private lateinit var mReference: DatabaseReference
    private lateinit var mUserReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            val email = register_email.text.toString().trim()
            val password = register_password.text.toString().trim()
            val name = register_name.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                if (password.length >= 6) {
                    register_progressBar.visible()
                    registerUser(name, email, password)
                } else {
                    register_password.error = "Şifre en az 6 karakter olmalı"
                }
            } else {
                if (email.isEmpty()) register_email.error = "Email boş geçilemez"
                if (password.isEmpty()) register_password.error = "Şifre boş geçilemez"
                if (name.isEmpty()) register_name.error = "İsim boş geçilemez"
            }
        }
        register_login_button.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    private fun registerUser(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(!it.isSuccessful) {
                Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show()
                register_progressBar.gone()
            } else {
                val currentUser = mAuth.currentUser
                val userId = currentUser?.uid

                mReference = mDatabase.reference
                mUserReference = mReference.child(Constants.CHILD_USERS).child(userId!!)

                val userMap = HashMap<String, String>()
                userMap["name"] = name
                userMap["profile_image"] = "no_image"
                userMap["status"] = "Hey there. I'm using ChatApp"

                mUserReference.setValue(userMap).addOnCompleteListener {
                    Toast.makeText(this, "BAŞARILI", Toast.LENGTH_SHORT).show()
                    register_progressBar.gone()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
            }
        }
    }






}
