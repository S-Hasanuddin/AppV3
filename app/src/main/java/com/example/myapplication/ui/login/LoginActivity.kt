package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up Login Button
        binding.btnLogin?.setOnClickListener {
            val email = binding.etEmail?.text.toString()
            val password = binding.etPassword?.text.toString()
            login(email, password)
        }

        // Set up Register Button
        binding.btnRegister?.setOnClickListener {
            val email = binding.etEmail?.text.toString()
            val password = binding.etPassword?.text.toString()
            register(email, password)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithEmail:success")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "createUserWithEmail:success")
                    Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("LoginActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
