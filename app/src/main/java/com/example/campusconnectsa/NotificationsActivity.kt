package com.example.campusconnectsa

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class StudentNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String,
    val time: String
)

class NotificationsActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var unreadSwitch: Switch
    private lateinit var listContainer: LinearLayout
    private lateinit var noNotifications: TextView
    private lateinit var unreadCount: TextView
    private lateinit var notificationCount: TextView

    private val preferences by lazy {
        getSharedPreferences("CampusConnectNotifications", MODE_PRIVATE)
    }

    private var selectedCategory = "All"
    private var unreadOnly = false

    private val notifications = listOf(
        StudentNotification(
            "n1",
            "Welcome to CampusConnect SA",
            "Your student life assistant is ready. Explore the available campus features.",
            "General",
            "Today"
        ),
        StudentNotification(
            "n2",
            "Academic reminder",
            "Remember to check your academic planner for upcoming assignments and assessments.",
            "Academic",
            "Today"
        ),
        StudentNotification(
            "n3",
            "New campus announcement",
            "There are new campus updates available in the Announcements section.",
            "Campus",
            "Yesterday"
        ),
        StudentNotification(
            "n4",
            "Safety reminder",
            "Keep emergency contact details accessible and report safety concerns.",
            "Safety",
            "Yesterday"
        ),
        StudentNotification(
            "n5",
            "Study resources available",
            "Explore the Study Resources section to find useful learning materials.",
            "Academic",
            "2 days ago"
        ),
        StudentNotification(
            "n6",
            "Transport information",
            "Check the Campus Transport section for available transport options.",
            "Transport",
            "2 days ago"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        categorySpinner = findViewById(R.id.spNotificationCategory)
        unreadSwitch = findViewById(R.id.switchUnreadNotifications)
        listContainer = findViewById(R.id.notificationListContainer)
        noNotifications = findViewById(R.id.tvNoNotifications)
        unreadCount = findViewById(R.id.tvUnreadCount)
        notificationCount = findViewById(R.id.tvNotificationCount)

        setupCategoryFilter()
        setupActions()
        displayNotifications()
    }

    private fun setupCategoryFilter() {
        val categories = listOf(
            "All",
            "General",
            "Academic",
            "Campus",
            "Safety",
            "Transport"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = categories[position]
                    displayNotifications()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        unreadSwitch.setOnCheckedChangeListener { _, checked ->
            unreadOnly = checked
            displayNotifications()
        }
    }

    private fun setupActions() {
        findViewById<Button>(
            R.id.btnMarkAllNotificationsRead
        ).setOnClickListener {
            val read = getStoredSet("readNotifications")
            val deleted = getStoredSet("deletedNotifications")

            notifications
                .filterNot { deleted.contains(it.id) }
                .forEach { read.add(it.id) }

            saveStoredSet("readNotifications", read)

            Toast.makeText(
                this,
                "All notifications marked as read",
                Toast.LENGTH_SHORT
            ).show()

            displayNotifications()
        }

        findViewById<Button>(
            R.id.btnClearNotifications
        ).setOnClickListener {
            val deleted = getStoredSet("deletedNotifications")
            notifications.forEach { deleted.add(it.id) }

            saveStoredSet("deletedNotifications", deleted)

            Toast.makeText(
                this,
                "Notifications cleared",
                Toast.LENGTH_SHORT
            ).show()

            displayNotifications()
        }
    }

    private fun getStoredSet(key: String): MutableSet<String> {
        return preferences.getStringSet(key, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveStoredSet(key: String, values: Set<String>) {
        preferences.edit()
            .putStringSet(key, values)
            .apply()
    }

    private fun displayNotifications() {
        listContainer.removeAllViews()

        val read = getStoredSet("readNotifications")
        val deleted = getStoredSet("deletedNotifications")

        val visibleNotifications = notifications.filter { notification ->
            val notDeleted = !deleted.contains(notification.id)
            val matchesCategory =
                selectedCategory == "All" ||
                        notification.category == selectedCategory

            val matchesUnread =
                !unreadOnly || !read.contains(notification.id)

            notDeleted && matchesCategory && matchesUnread
        }

        val totalUnread = notifications.count {
            !read.contains(it.id) && !deleted.contains(it.id)
        }

        unreadCount.text = if (totalUnread == 1) {
            "You have 1 unread notification"
        } else {
            "You have $totalUnread unread notifications"
        }

        notificationCount.text = "${visibleNotifications.size} items"

        noNotifications.visibility =
            if (visibleNotifications.isEmpty()) View.VISIBLE else View.GONE

        visibleNotifications.forEach { notification ->
            createNotificationCard(
                notification,
                read.contains(notification.id)
            )
        }
    }

    private fun createNotificationCard(
        notification: StudentNotification,
        isRead: Boolean
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = getDrawable(R.drawable.bg_notification_card)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 14
        card.layoutParams = params

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = when (notification.category) {
                "Academic" -> "📚"
                "Campus" -> "🏫"
                "Safety" -> "🛡️"
                "Transport" -> "🚌"
                else -> "🔔"
            }
            textSize = 23f
        }

        header.addView(icon)

        val category = TextView(this).apply {
            text = notification.category.uppercase()
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(2, 132, 199))
            setPadding(10, 0, 0, 0)
        }

        header.addView(category)

        val spacer = View(this)
        header.addView(
            spacer,
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        val status = TextView(this).apply {
            text = if (isRead) "READ" else "NEW"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(
                if (isRead) Color.GRAY else Color.rgb(2, 132, 199)
            )
        }

        header.addView(status)
        card.addView(header)

        val title = TextView(this).apply {
            text = notification.title
            textSize = 16f
            setTextColor(Color.rgb(15, 41, 66))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 12, 0, 5)
        }

        card.addView(title)

        val message = TextView(this).apply {
            text = notification.message
            textSize = 13f
            setTextColor(Color.rgb(100, 116, 139))
        }

        card.addView(message)

        val time = TextView(this).apply {
            text = notification.time
            textSize = 11f
            setTextColor(Color.rgb(148, 163, 184))
            setPadding(0, 10, 0, 8)
        }

        card.addView(time)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val readButton = Button(this).apply {
            text = if (isRead) "Mark unread" else "Mark as read"
            textSize = 11f

            setOnClickListener {
                val readItems = getStoredSet("readNotifications")

                if (readItems.contains(notification.id)) {
                    readItems.remove(notification.id)
                } else {
                    readItems.add(notification.id)
                }

                saveStoredSet("readNotifications", readItems)
                displayNotifications()
            }
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            textSize = 11f
            setTextColor(Color.rgb(220, 38, 38))

            setOnClickListener {
                val deleted = getStoredSet("deletedNotifications")
                deleted.add(notification.id)
                saveStoredSet("deletedNotifications", deleted)
                displayNotifications()

                Toast.makeText(
                    this@NotificationsActivity,
                    "Notification deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        actions.addView(readButton)
        actions.addView(deleteButton)
        card.addView(actions)

        listContainer.addView(card)
    }
}