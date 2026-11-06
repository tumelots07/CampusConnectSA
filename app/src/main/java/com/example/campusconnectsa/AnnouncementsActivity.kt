package com.example.campusconnectsa

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class CampusAnnouncement(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val date: String,
    val pinned: Boolean
)

class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var listContainer: LinearLayout
    private lateinit var noAnnouncements: TextView
    private lateinit var countText: TextView
    private lateinit var summaryText: TextView
    private lateinit var unreadSwitch: Switch
    private lateinit var savedSwitch: Switch

    private val preferences by lazy {
        getSharedPreferences("CampusConnectAnnouncements", MODE_PRIVATE)
    }

    private var selectedCategory = "All"
    private var unreadOnly = false
    private var savedOnly = false

    private val announcements = listOf(
        CampusAnnouncement(
            "a1",
            "Registration Information",
            "Students are reminded to check their registration status and ensure that all required documents have been submitted.",
            "Academic",
            "5 November 2026",
            true
        ),
        CampusAnnouncement(
            "a2",
            "Examination Preparation",
            "Remember to check your examination timetable and prepare for upcoming assessments.",
            "Academic",
            "4 November 2026",
            true
        ),
        CampusAnnouncement(
            "a3",
            "Library Operating Hours",
            "The campus library provides study facilities. Please confirm the latest opening and closing times with the library.",
            "Campus",
            "3 November 2026",
            false
        ),
        CampusAnnouncement(
            "a4",
            "Campus Safety Reminder",
            "Students are encouraged to remain alert, protect their belongings and report safety concerns.",
            "Urgent",
            "2 November 2026",
            true
        ),
        CampusAnnouncement(
            "a5",
            "Student Support Services",
            "Students who need academic or personal support can contact the relevant student support office.",
            "Campus",
            "1 November 2026",
            false
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        searchInput = findViewById(R.id.etSearchAnnouncement)
        listContainer = findViewById(R.id.announcementListContainer)
        noAnnouncements = findViewById(R.id.tvNoAnnouncements)
        countText = findViewById(R.id.tvAnnouncementCount)
        summaryText = findViewById(R.id.tvAnnouncementSummary)
        unreadSwitch = findViewById(R.id.switchUnreadAnnouncements)
        savedSwitch = findViewById(R.id.switchSavedAnnouncements)

        setupFilters()
        displayAnnouncements()

        findViewById<Button>(R.id.btnMarkAllRead).setOnClickListener {
            markAllAsRead()
        }
    }

    private fun setupFilters() {
        findViewById<Button>(R.id.btnAllAnnouncements).setOnClickListener {
            selectCategory("All")
        }

        findViewById<Button>(R.id.btnAcademicAnnouncements).setOnClickListener {
            selectCategory("Academic")
        }

        findViewById<Button>(R.id.btnCampusAnnouncements).setOnClickListener {
            selectCategory("Campus")
        }

        findViewById<Button>(R.id.btnUrgentAnnouncements).setOnClickListener {
            selectCategory("Urgent")
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                displayAnnouncements()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        unreadSwitch.setOnCheckedChangeListener { _, checked ->
            unreadOnly = checked
            displayAnnouncements()
        }

        savedSwitch.setOnCheckedChangeListener { _, checked ->
            savedOnly = checked
            displayAnnouncements()
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        displayAnnouncements()
    }

    private fun getStoredSet(key: String): MutableSet<String> {
        return preferences.getStringSet(key, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveSet(key: String, values: Set<String>) {
        preferences.edit().putStringSet(key, values).apply()
    }

    private fun displayAnnouncements() {
        listContainer.removeAllViews()

        val readItems = getStoredSet("readItems")
        val savedItems = getStoredSet("savedItems")
        val search = searchInput.text.toString().trim().lowercase()

        val filtered = announcements
            .filter { item ->
                val matchesSearch =
                    item.title.lowercase().contains(search) ||
                            item.description.lowercase().contains(search) ||
                            item.category.lowercase().contains(search)

                val matchesCategory =
                    selectedCategory == "All" ||
                            item.category == selectedCategory

                val matchesUnread =
                    !unreadOnly || !readItems.contains(item.id)

                val matchesSaved =
                    !savedOnly || savedItems.contains(item.id)

                matchesSearch && matchesCategory &&
                        matchesUnread && matchesSaved
            }
            .sortedByDescending { it.pinned }

        countText.text = "${filtered.size} updates"

        val unreadCount = announcements.count {
            !readItems.contains(it.id)
        }

        summaryText.text =
            "$unreadCount unread announcements. Check the latest campus updates."

        noAnnouncements.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        filtered.forEach { item ->
            createAnnouncementCard(
                item,
                readItems.contains(item.id),
                savedItems.contains(item.id)
            )
        }
    }

    private fun createAnnouncementCard(
        item: CampusAnnouncement,
        isRead: Boolean,
        isSaved: Boolean
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = getDrawable(R.drawable.bg_announcement_card)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 14
        card.layoutParams = params

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val category = TextView(this).apply {
            text = item.category.uppercase()
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(
                when (item.category) {
                    "Urgent" -> Color.rgb(185, 28, 28)
                    "Academic" -> Color.rgb(2, 132, 199)
                    else -> Color.rgb(22, 101, 52)
                }
            )
        }

        topRow.addView(category)

        if (item.pinned) {
            val pinned = TextView(this).apply {
                text = "  •  PINNED"
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(180, 83, 9))
            }
            topRow.addView(pinned)
        }

        val spacer = View(this)
        topRow.addView(
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
        topRow.addView(status)
        card.addView(topRow)

        val title = TextView(this).apply {
            text = item.title
            textSize = 17f
            setTextColor(Color.rgb(15, 41, 66))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 12, 0, 5)
        }
        card.addView(title)

        val description = TextView(this).apply {
            text = item.description
            textSize = 13f
            setTextColor(Color.rgb(100, 116, 139))
        }
        card.addView(description)

        val date = TextView(this).apply {
            text = "Posted: ${item.date}"
            textSize = 11f
            setTextColor(Color.rgb(148, 163, 184))
            setPadding(0, 10, 0, 4)
        }
        card.addView(date)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val readButton = Button(this).apply {
            text = if (isRead) "Mark unread" else "Mark as read"
            textSize = 11f
            setOnClickListener {
                val read = getStoredSet("readItems")
                if (read.contains(item.id)) {
                    read.remove(item.id)
                } else {
                    read.add(item.id)
                }
                saveSet("readItems", read)
                displayAnnouncements()
            }
        }

        val saveButton = Button(this).apply {
            text = if (isSaved) "★ Saved" else "☆ Save"
            textSize = 11f
            setTextColor(Color.rgb(2, 132, 199))
            setOnClickListener {
                val saved = getStoredSet("savedItems")
                if (saved.contains(item.id)) {
                    saved.remove(item.id)
                    Toast.makeText(
                        this@AnnouncementsActivity,
                        "Announcement removed from saved items",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saved.add(item.id)
                    Toast.makeText(
                        this@AnnouncementsActivity,
                        "Announcement saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                saveSet("savedItems", saved)
                displayAnnouncements()
            }
        }

        actions.addView(readButton)
        actions.addView(saveButton)
        card.addView(actions)

        listContainer.addView(card)
    }

    private fun markAllAsRead() {
        val read = getStoredSet("readItems")
        announcements.forEach { read.add(it.id) }
        saveSet("readItems", read)

        Toast.makeText(
            this,
            "All announcements marked as read",
            Toast.LENGTH_SHORT
        ).show()

        displayAnnouncements()
    }
}