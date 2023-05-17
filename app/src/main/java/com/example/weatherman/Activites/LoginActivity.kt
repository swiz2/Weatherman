package com.example.weatherman.Activites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherman.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var emailTextView: TextInputEditText
    private lateinit var passwordTextView: TextInputEditText
    private lateinit var emailTextView1: TextInputEditText
    private lateinit var passwordTextViewconfirm1: TextInputEditText
    private lateinit var passwordTextViewconfirm2: TextInputEditText

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailTextView = findViewById(R.id.eMail)
        passwordTextView = findViewById(R.id.passwords)
        emailTextView1 = findViewById(R.id.teMails)
        passwordTextViewconfirm1 = findViewById(R.id.passwordss)
        passwordTextViewconfirm2 = findViewById(R.id.passwords01)

        val signUp = findViewById<TextView>(R.id.signUp)
        val logIn = findViewById<TextView>(R.id.logIn)
        val signUpLayout = findViewById<LinearLayout>(R.id.siUpLayout)
        val logInLayout = findViewById<LinearLayout>(R.id.logInLayout)
        val signInButton = findViewById<Button>(R.id.sign_In)
        val logInButton = findViewById<Button>(R.id.loginBtn)

        auth = FirebaseAuth.getInstance()

        signUp.setOnClickListener {
            signUp.background = resources.getDrawable(R.drawable.switch_trcks, null)
            signUp.setTextColor(resources.getColor(R.color.textColor, null))
            logInButton.visibility = View.GONE
            logIn.background = null
            signUpLayout.visibility = View.VISIBLE
            logInLayout.visibility = View.GONE
            logIn.setTextColor(resources.getColor(R.color.pinkColor, null))
        }
        logIn.setOnClickListener {
            signUp.background = null
            signUp.setTextColor(resources.getColor(R.color.pinkColor, null))
            logIn.background = resources.getDrawable(R.drawable.switch_trcks, null)
            signUpLayout.visibility = View.GONE
            logInButton.visibility = View.VISIBLE
            logInLayout.visibility = View.VISIBLE
            logIn.setTextColor(resources.getColor(R.color.textColor, null))
        }

        logInButton.setOnClickListener {
            val email = emailTextView.text.toString()
            val password = passwordTextView.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        Toast.makeText(this, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                        // User logged in successfully
                        user?.let {
                            // Perform any necessary actions after successful login
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish() // Optional: finish the LoginActivity
                        }
                    } else {
                        // Login failed
                        // Display an error message or perform any necessary actions
                        Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signInButton.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val temail = emailTextView1.text.toString()
        val epass = passwordTextViewconfirm1.text.toString()
        val confirmpass = passwordTextViewconfirm2.text.toString()
        if (temail.isBlank() || epass.isBlank() || confirmpass.isBlank()) {
            Toast.makeText(this, "Email and password can't be blank", Toast.LENGTH_LONG).show()
            return
        } else if (epass != confirmpass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(temail, epass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Signed up successfully", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to create an account", Toast.LENGTH_LONG).show()
            }
        }





    }
}