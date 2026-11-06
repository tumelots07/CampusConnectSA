
package com.example.campusconnectsa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusconnectsa.data.ApiClient
import com.example.campusconnectsa.data.SessionManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvModuleCount: TextView
    private lateinit var tvTaskCount: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressStudy: ProgressBar

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)

        initializeViews()
        displayWelcomeMessage()
        setupNavigation()
        resetAcademicOverview()
    }

    override fun onResume() {
        super.onResume()

        // Refresh progress whenever the dashboard becomes visible.
        loadAcademicOverview()
    }

    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvModuleCount = findViewById(R.id.tvModuleCount)
        tvTaskCount = findViewById(R.id.tvTaskCount)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        progressStudy = findViewById(R.id.progressStudy)
    }

    private fun displayWelcomeMessage() {
        val fullName = sessionManager.getFullName()
            ?: getSharedPreferences(
                "CampusConnect",
                MODE_PRIVATE
            ).getString("firstName", "Student")
            ?: "Student"

        val firstName = fullName.trim().substringBefore(" ")

        tvWelcome.text = "Welcome, $firstName!"
    }

    private fun setupNavigation() {

        // Academic Planner
        findViewById<View>(R.id.btnAcademics).setOnClickListener {
            openScreen(AcademicPlannerActivity::class.java)
        }

        findViewById<View>(R.id.tvUpcomingArrow).setOnClickListener {
            openScreen(AcademicPlannerActivity::class.java)
        }

        findViewById<View>(R.id.navAcademics).setOnClickListener {
            openScreen(AcademicPlannerActivity::class.java)
        }

        // Study Resources
        findViewById<View>(R.id.btnResources).setOnClickListener {
            openScreen(ResourcesActivity::class.java)
        }

        // Campus Services
        findViewById<View>(R.id.btnServices).setOnClickListener {
            openScreen(CampusServicesActivity::class.java)
        }

        // Transport
        findViewById<View>(R.id.btnTransport).setOnClickListener {
            openScreen(TransportActivity::class.java)
        }

        // Campus Safety
        findViewById<View>(R.id.btnSafety).setOnClickListener {
            openScreen(SafetyActivity::class.java)
        }

        // Announcements
        findViewById<View>(R.id.btnAnnouncements).setOnClickListener {
            openScreen(AnnouncementsActivity::class.java)
        }

        // Notifications
        findViewById<View>(R.id.btnNotifications).setOnClickListener {
            openScreen(NotificationsActivity::class.java)
        }

        findViewById<View>(R.id.btnNotificationsQuick).setOnClickListener {
            openScreen(NotificationsActivity::class.java)
        }

        findViewById<View>(R.id.navNotifications).setOnClickListener {
            openScreen(NotificationsActivity::class.java)
        }

        // Bottom navigation
        findViewById<View>(R.id.navHome).setOnClickListener {
            // Already on the dashboard.
        }

        findViewById<View>(R.id.navCampus).setOnClickListener {
            openScreen(CampusServicesActivity::class.java)
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            openScreen(SettingsActivity::class.java)
        }
    }

    private fun resetAcademicOverview() {
        tvModuleCount.text = "0"
        tvTaskCount.text = "0"
        tvProgressPercent.text = "0%"
        progressStudy.progress = 0
    }

    private fun loadAcademicOverview() {

        val token = sessionManager.getToken()

        if (token.isNullOrBlank()) {
            resetAcademicOverview()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTasks(
                    "Bearer $token"
                )

                if (response.isSuccessful) {

                    val tasks = response.body()?.tasks.orEmpty()

                    // Count distinct modules.
                    val moduleCount = tasks
                        .mapNotNull { it.module?.trim() }
                        .filter { it.isNotEmpty() }
                        .distinctBy { it.lowercase() }
                        .size

                    // Count activities not yet completed.
                    val pendingCount = tasks.count {
                        !it.completed
                    }

                    // Calculate actual completion percentage.
                    val completedCount = tasks.count {
                        it.completed
                    }

                    val progress = if (tasks.isEmpty()) {
                        0
                    } else {
                        (completedCount * 100) / tasks.size
                    }

                    tvModuleCount.text = moduleCount.toString()
                    tvTaskCount.text = pendingCount.toString()
                    tvProgressPercent.text = "$progress%"
                    progressStudy.progress = progress

                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Could not load academic progress.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (exception: Exception) {

                Toast.makeText(
                    this@DashboardActivity,
                    "Unable to connect to the academic server.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openScreen(activity: Class<*>) {
        startActivity(Intent(this, activity))
    }
}
