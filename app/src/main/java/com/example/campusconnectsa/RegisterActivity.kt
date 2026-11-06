package com.example.campusconnectsa

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusconnectsa.data.ApiClient
import com.example.campusconnectsa.data.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firstName = findViewById<EditText>(R.id.etFirstName)
        val surname = findViewById<EditText>(R.id.etSurname)
        val studentNumber = findViewById<EditText>(R.id.etStudentNumber)
        val email = findViewById<EditText>(R.id.etRegisterEmail)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        val registerButton = findViewById<Button>(R.id.btnRegister)
        val loginLink = findViewById<TextView>(R.id.tvBackToLogin)

        registerButton.setOnClickListener {

            val first = firstName.text.toString().trim()
            val last = surname.text.toString().trim()
            val student = studentNumber.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val pass = password.text.toString()
            val confirm = confirmPassword.text.toString()

            when {
                first.isEmpty() -> {
                    firstName.error = "Enter your first name"
                }

                last.isEmpty() -> {
                    surname.error = "Enter your surname"
                }

                student.isEmpty() -> {
                    studentNumber.error = "Enter your student number"
                }

                userEmail.isEmpty() -> {
                    email.error = "Enter your email address"
                }

                !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                    email.error = "Enter a valid email address"
                }

                pass.length < 8 -> {
                    password.error = "Use at least 8 characters"
                }

                !pass.any { it.isUpperCase() } -> {
                    password.error = "Include at least one uppercase letter (A-Z)"
                }

                !pass.any { it.isLowerCase() } -> {
                    password.error = "Include at least one lowercase letter (a-z)"
                }

                !pass.any { it.isDigit() } -> {
                    password.error = "Include at least one number (0-9)"
                }

                !pass.any { !it.isLetterOrDigit() } -> {
                    password.error = "Include at least one special character (!@#$%)"
                }

                confirm != pass -> {
                    confirmPassword.error = "Passwords do not match"
                }

                else -> {
                    registerButton.isEnabled = false
                    registerButton.text = "Creating account..."

                    lifecycleScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                ApiClient.apiService.register(
                                    RegisterRequest(
                                        fullName = "$first $last",
                                        email = userEmail,
                                        studentNumber = student,
                                        password = pass,
                                        confirmPassword = confirm
                                    )
                                )
                            }

                            if (response.isSuccessful) {
                                val result = response.body()

                                if (result?.token != null && result.user != null) {

                                    // Save authentication session
                                    getSharedPreferences(
                                        "CampusConnectSession",
                                        MODE_PRIVATE
                                    ).edit()
                                        .putString("token", result.token)
                                        .putString("userId", result.user.id)
                                        .putString("fullName", result.user.fullName)
                                        .putString("email", result.user.email)
                                        .putString("role", result.user.role)
                                        .apply()

                                    // Save details used by the dashboard
                                    getSharedPreferences(
                                        "CampusConnect",
                                        MODE_PRIVATE
                                    ).edit()
                                        .putString(
                                            "firstName",
                                            result.user.fullName.substringBefore(" ")
                                        )
                                        .putString("email", result.user.email)
                                        .apply()

                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Account created successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this@RegisterActivity,
                                            DashboardActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Account created, but the server response was incomplete. Please sign in.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } else {
                                val errorMessage = try {
                                    val errorJson = JSONObject(
                                        response.errorBody()?.string() ?: "{}"
                                    )
                                    errorJson.optString(
                                        "message",
                                        "Registration failed. Please check your details."
                                    )
                                } catch (e: Exception) {
                                    "Registration failed. Please try again."
                                }

                                Toast.makeText(
                                    this@RegisterActivity,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Could not connect to the server. Check your Wi-Fi connection.",
                                Toast.LENGTH_LONG
                            ).show()

                        } finally {
                            registerButton.isEnabled = true
                            registerButton.text = "Create Account"
                        }
                    }
                }
            }
        }

        loginLink.setOnClickListener {
            finish()
        }
    }
}