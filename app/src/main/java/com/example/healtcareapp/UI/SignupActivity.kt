package com.example.healtcareapp.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.healtcareapp.R
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmailSignup: EditText
    private lateinit var etPasswordSignup: EditText
    private lateinit var btnSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()


        etEmailSignup = findViewById(R.id.etEmailSignup)
        etPasswordSignup = findViewById(R.id.etPasswordSignup)
        btnSignup = findViewById(R.id.btnSignup)
        val btnGtLogin = findViewById<Button>(R.id.btnGtLogin)

        btnSignup.setOnClickListener {
            val email = etEmailSignup.text.toString()
            val password = etPasswordSignup.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        btnGtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}