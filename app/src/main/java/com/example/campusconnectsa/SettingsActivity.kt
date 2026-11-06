
package com.example.campusconnectsa

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusconnectsa.data.ApiClient
import com.example.campusconnectsa.data.ProfileUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private val preferences by lazy {
        getSharedPreferences("CampusConnectSettings", MODE_PRIVATE)
    }

    private val loginPreferences by lazy {
        getSharedPreferences("CampusConnect", MODE_PRIVATE)
    }

    private val sessionPreferences by lazy {
        getSharedPreferences("CampusConnectSession", MODE_PRIVATE)
    }

    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileInitials: TextView

    private lateinit var editContainer: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var studentNumberInput: EditText

    private lateinit var notificationsSwitch: Switch
    private lateinit var darkModeSwitch: Switch
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        connectViews()
        loadProfile()
        setupPreferences()
        setupButtons()
    }

    private fun connectViews() {
        profileName = findViewById(R.id.tvProfileName)
        profileEmail = findViewById(R.id.tvProfileEmail)
        profileInitials = findViewById(R.id.tvProfileInitials)

        editContainer = findViewById(R.id.profileEditContainer)
        nameInput = findViewById(R.id.etProfileName)
        emailInput = findViewById(R.id.etProfileEmail)
        studentNumberInput = findViewById(R.id.etStudentNumber)

        notificationsSwitch = findViewById(R.id.switchNotifications)
        darkModeSwitch = findViewById(R.id.switchDarkMode)
        languageSpinner = findViewById(R.id.spLanguage)
    }

    private fun loadProfile() {
        // Load details belonging to the currently logged-in user.
        val sessionName = sessionPreferences.getString("fullName", null)
        val sessionEmail = sessionPreferences.getString("email", null)
        val sessionStudentNumber =
            sessionPreferences.getString("studentNumber", null)

        val name = sessionName
            ?.takeIf { it.isNotBlank() }
            ?: loginPreferences.getString("fullName", null)
                ?.takeIf { it.isNotBlank() }
            ?: loginPreferences.getString("firstName", null)
                ?.takeIf { it.isNotBlank() }
            ?: preferences.getString("profileName", "Student")
            ?: "Student"

        val email = sessionEmail
            ?.takeIf { it.isNotBlank() }
            ?: loginPreferences.getString("email", null)
                ?.takeIf { it.isNotBlank() }
            ?: preferences.getString("profileEmail", "")
            ?: ""

        val studentNumber = sessionStudentNumber
            ?.takeIf { it.isNotBlank() }
            ?: preferences.getString("studentNumber", "")
            ?: ""

        profileName.text = name
        profileEmail.text = email
        profileInitials.text = getInitials(name)

        nameInput.setText(name)
        emailInput.setText(email)
        studentNumberInput.setText(studentNumber)

        // Update the local profile cache.
        preferences.edit()
            .putString("profileName", name)
            .putString("profileEmail", email)
            .putString("studentNumber", studentNumber)
            .apply()
    }

    private fun getInitials(name: String): String {
        return name.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "ST" }
    }

    private fun setupPreferences() {

        // Notifications
        notificationsSwitch.isChecked =
            preferences.getBoolean("notificationsEnabled", true)

        notificationsSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.edit()
                .putBoolean("notificationsEnabled", checked)
                .apply()

            Toast.makeText(
                this,
                if (checked) "Notifications enabled"
                else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Dark mode
        darkModeSwitch.isChecked =
            preferences.getBoolean("darkModeEnabled", false)

        darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.edit()
                .putBoolean("darkModeEnabled", checked)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (checked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }

        // Supported languages
        val languages = listOf(
            "English",
            "isiZulu",
            "isiXhosa",
            "Sesotho",
            "Afrikaans",
            "Setswana"
        )

        val languageCodes = mapOf(
            "English" to "en",
            "isiZulu" to "zu",
            "isiXhosa" to "xh",
            "Sesotho" to "st",
            "Afrikaans" to "af",
            "Setswana" to "tn"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = adapter

        // Restore the saved language.
        val savedLanguage = preferences.getString(
            "preferredLanguage",
            "English"
        ) ?: "English"

        val languagePosition = languages.indexOf(savedLanguage)

        languageSpinner.setSelection(
            if (languagePosition >= 0) languagePosition else 0,
            false
        )

        // Apply the saved language when the activity opens.
        val savedLanguageCode = languageCodes[savedLanguage] ?: "en"

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(savedLanguageCode)
        )

        /*
         * Important fix:
         * Do not skip the first Spinner callback.
         * Compare the selected language with the saved language instead.
         * This prevents the initial Spinner event from consuming the
         * user's first actual selection.
         */
        languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position !in languages.indices) return

                    val selectedLanguage = languages[position]

                    val currentLanguage = preferences.getString(
                        "preferredLanguage",
                        "English"
                    ) ?: "English"

                    // Ignore the initial callback if nothing changed.
                    if (selectedLanguage == currentLanguage) return

                    val selectedCode =
                        languageCodes[selectedLanguage] ?: "en"

                    // Save the selected language.
                    preferences.edit()
                        .putString("preferredLanguage", selectedLanguage)
                        .apply()

                    // Apply the new language immediately.
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(selectedCode)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed.
                }
            }
    }

    private fun setupButtons() {

        findViewById<Button>(R.id.btnEditProfile)
            .setOnClickListener {
                editContainer.visibility =
                    if (editContainer.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }

        findViewById<Button>(R.id.btnCancelProfile)
            .setOnClickListener {
                loadProfile()
                editContainer.visibility = View.GONE
            }

        findViewById<Button>(R.id.btnSaveProfile)
            .setOnClickListener {
                saveProfile()
            }

        findViewById<Button>(R.id.btnLogout)
            .setOnClickListener {
                confirmLogout()
            }
    }

    private fun saveProfile() {

        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val studentNumber = studentNumberInput.text.toString().trim()

        when {
            name.length < 2 -> {
                nameInput.error = "Enter your full name"
                nameInput.requestFocus()
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Enter a valid email address"
                emailInput.requestFocus()
                return
            }
        }

        val token = sessionPreferences.getString("token", null)

        if (token.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Please log in again to update your profile.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val saveButton = findViewById<Button>(R.id.btnSaveProfile)

        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.updateProfile(
                        "Bearer $token",
                        ProfileUpdateRequest(
                            name,
                            email,
                            studentNumber
                        )
                    )
                }

                if (response.isSuccessful) {

                    val updatedUser = response.body()?.user

                    if (updatedUser != null) {

                        // Update the active session.
                        sessionPreferences.edit()
                            .putString("fullName", updatedUser.fullName)
                            .putString("email", updatedUser.email)
                            .putString(
                                "studentNumber",
                                updatedUser.studentNumber
                            )
                            .apply()

                        // Update the profile cache.
                        preferences.edit()
                            .putString("profileName", updatedUser.fullName)
                            .putString("profileEmail", updatedUser.email)
                            .putString(
                                "studentNumber",
                                updatedUser.studentNumber
                            )
                            .apply()

                        // Update login details.
                        loginPreferences.edit()
                            .putString(
                                "firstName",
                                updatedUser.fullName.substringBefore(" ")
                            )
                            .putString("email", updatedUser.email)
                            .apply()

                        loadProfile()
                        editContainer.visibility = View.GONE

                        Toast.makeText(
                            this@SettingsActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Incomplete server response.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    val message = try {
                        JSONObject(
                            response.errorBody()?.string() ?: "{}"
                        ).optString(
                            "message",
                            "Profile update failed."
                        )
                    } catch (e: Exception) {
                        "Profile update failed."
                    }

                    Toast.makeText(
                        this@SettingsActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@SettingsActivity,
                    "Unable to connect to the server.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                saveButton.isEnabled = true
                saveButton.text = "Save Changes"
            }
        }
    }

    private fun confirmLogout() {

        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage(
                "Are you sure you want to log out of CampusConnect SA?"
            )
            .setPositiveButton("Log out") { _, _ ->

                loginPreferences.edit()
                    .clear()
                    .apply()

                sessionPreferences.edit()
                    .clear()
                    .apply()

                preferences.edit()
                    .remove("profileName")
                    .remove("profileEmail")
                    .remove("studentNumber")
                    .apply()

                val intent = Intent(this, LoginActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
