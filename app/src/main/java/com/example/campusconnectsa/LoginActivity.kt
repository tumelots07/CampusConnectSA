
package com.example.campusconnectsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.campusconnectsa.data.ApiClient
import com.example.campusconnectsa.data.AuthResponse
import com.example.campusconnectsa.data.GoogleLoginRequest
import com.example.campusconnectsa.data.LoginRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CampusConnectGoogle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)

        val loginButton = findViewById<Button>(R.id.btnLogin)
        val googleButton = findViewById<Button>(R.id.btnGoogle)
        val registerLink = findViewById<TextView>(R.id.tvRegister)
        val forgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // ==========================================
        // NORMAL EMAIL AND PASSWORD LOGIN
        // ==========================================

        loginButton.setOnClickListener {

            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString()

            when {
                userEmail.isEmpty() -> {
                    email.error = "Please enter your email"
                }

                !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                    email.error = "Enter a valid email address"
                }

                userPassword.isEmpty() -> {
                    password.error = "Please enter your password"
                }

                else -> {

                    loginButton.isEnabled = false
                    loginButton.text = "Signing in..."

                    lifecycleScope.launch {

                        try {
                            val response = withContext(Dispatchers.IO) {
                                ApiClient.apiService.login(
                                    LoginRequest(
                                        userEmail,
                                        userPassword
                                    )
                                )
                            }

                            if (response.isSuccessful) {

                                val result = response.body()

                                if (result?.token != null && result.user != null) {

                                    saveSession(result)

                                    showDashboard(
                                        "Welcome, ${result.user.fullName}!"
                                    )

                                } else {
                                    showMessage("Invalid response from server.")
                                }

                            } else {
                                showMessage(
                                    getErrorMessage(
                                        response.errorBody()?.string()
                                    )
                                )
                            }

                        } catch (e: Exception) {

                            Log.e(TAG, "Normal login failed", e)

                            showMessage(
                                "Could not connect to the server. Check your connection."
                            )

                        } finally {

                            loginButton.isEnabled = true
                            loginButton.text = "Login"
                        }
                    }
                }
            }
        }

        // ==========================================
        // GOOGLE SIGN-IN
        // ==========================================

        googleButton.setOnClickListener {

            googleButton.isEnabled = false
            googleButton.text = "Connecting to Google..."

            lifecycleScope.launch {

                try {

                    Log.d(TAG, "Starting Google Sign-In")

                    // STEP 1: Create Credential Manager
                    val credentialManager =
                        CredentialManager.create(this@LoginActivity)

                    // STEP 2: Configure Google account selection
                    val googleIdOption =
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(
                                getString(R.string.default_web_client_id)
                            )
                            .setAutoSelectEnabled(false)
                            .build()

                    // STEP 3: Build credential request
                    val request =
                        GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                    Log.d(TAG, "Opening Google account picker")

                    // STEP 4: Display Google account picker
                    val credentialResponse =
                        credentialManager.getCredential(
                            context = this@LoginActivity,
                            request = request
                        )

                    val credential = credentialResponse.credential

                    // STEP 5: Validate Google credential
                    if (
                        credential.type !=
                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        throw IllegalStateException(
                            "Google did not return a valid ID credential."
                        )
                    }

                    // STEP 6: Extract Google ID token
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    Log.d(TAG, "Google ID token received")

                    // STEP 7: Authenticate with Firebase
                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(
                            googleCredential.idToken,
                            null
                        )

                    val firebaseResult =
                        FirebaseAuth.getInstance()
                            .signInWithCredential(firebaseCredential)
                            .await()

                    val firebaseUser = firebaseResult.user
                        ?: throw IllegalStateException(
                            "Firebase could not identify the Google account."
                        )

                    Log.d(
                        TAG,
                        "Firebase authentication successful: ${firebaseUser.uid}"
                    )

                    // STEP 8: Obtain Firebase ID token
                    val firebaseIdToken =
                        firebaseUser.getIdToken(true)
                            .await()
                            .token
                            ?: throw IllegalStateException(
                                "Could not obtain Firebase authentication token."
                            )

                    Log.d(TAG, "Firebase ID token obtained")

                    // STEP 9: Send Firebase token to backend
                    val response = withContext(Dispatchers.IO) {

                        ApiClient.apiService.googleLogin(
                            GoogleLoginRequest(firebaseIdToken)
                        )
                    }

                    // STEP 10: Process backend response
                    if (response.isSuccessful) {

                        val result = response.body()

                        if (result?.token != null && result.user != null) {

                            Log.d(TAG, "Backend Google login successful")

                            saveSession(result)

                            showDashboard(
                                "Welcome, ${result.user.fullName}!"
                            )

                        } else {

                            showMessage(
                                "The server returned an incomplete login response."
                            )
                        }

                    } else {

                        val error = response.errorBody()?.string()

                        Log.e(TAG, "Backend error: $error")

                        showMessage(
                            getErrorMessage(error)
                        )
                    }

                } catch (e: GetCredentialException) {

                    Log.e(
                        TAG,
                        "Google Credential Manager failed: ${e.javaClass.simpleName}",
                        e
                    )

                    showMessage(
                        "Google account selection failed: " +
                                (e.message ?: e.javaClass.simpleName)
                    )

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "Google authentication failed: ${e.javaClass.simpleName}",
                        e
                    )

                    showMessage(
                        "Google Sign-In failed: " +
                                (e.message ?: e.javaClass.simpleName)
                    )

                } finally {

                    googleButton.isEnabled = true
                    googleButton.text = "Continue with Google"
                }
            }
        }

        // ==========================================
        // REGISTRATION
        // ==========================================

        registerLink.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // ==========================================
        // FORGOT PASSWORD
        // ==========================================

        forgotPassword.setOnClickListener {

            Toast.makeText(
                this,
                "Password recovery will be implemented with authentication.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ==========================================
    // SAVE LOGIN SESSION
    // ==========================================

    private fun saveSession(result: AuthResponse) {

        val user = result.user ?: return
        val token = result.token ?: return

        getSharedPreferences(
            "CampusConnectSession",
            MODE_PRIVATE
        ).edit()
            .putString("token", token)
            .putString("userId", user.id)
            .putString("fullName", user.fullName)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()

        getSharedPreferences(
            "CampusConnect",
            MODE_PRIVATE
        ).edit()
            .putString(
                "firstName",
                user.fullName.substringBefore(" ")
            )
            .putString("email", user.email)
            .apply()
    }

    // ==========================================
    // OPEN DASHBOARD
    // ==========================================

    private fun showDashboard(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        startActivity(
            Intent(this, DashboardActivity::class.java)
        )

        finish()
    }

    // ==========================================
    // READ BACKEND ERROR
    // ==========================================

    private fun getErrorMessage(errorBody: String?): String {

        return try {

            JSONObject(errorBody ?: "{}").optString(
                "message",
                "Login failed. Please try again."
            )

        } catch (e: Exception) {

            "Login failed. Please try again."
        }
    }

    // ==========================================
    // DISPLAY MESSAGE
    // ==========================================

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
