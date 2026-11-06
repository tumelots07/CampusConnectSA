
package com.example.campusconnectsa

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class StudyResource(
    val title: String,
    val description: String,
    val category: String,
    val url: String
)

class ResourcesActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var resourceContainer: LinearLayout
    private lateinit var noResources: TextView
    private lateinit var resourceCount: TextView
    private lateinit var savedSwitch: Switch

    private var selectedCategory = "All"
    private var showSavedOnly = false

    private val preferences by lazy {
        getSharedPreferences("CampusConnectResources", MODE_PRIVATE)
    }

    private val savedResources = mutableSetOf<String>()

    private val resources = listOf(
        StudyResource(
            "Siyavula Textbooks",
            "South African textbooks, exercises and learning materials.",
            "Textbooks",
            "https://www.siyavula.com/"
        ),
        StudyResource(
            "OpenStax Textbooks",
            "Free digital textbooks covering different academic subjects.",
            "Textbooks",
            "https://openstax.org/subjects"
        ),
        StudyResource(
            "Khan Academy",
            "Educational lessons, practice exercises and videos.",
            "Videos",
            "https://www.khanacademy.org/"
        ),
        StudyResource(
            "MIT OpenCourseWare",
            "Access free learning materials from MIT courses.",
            "Courses",
            "https://ocw.mit.edu/"
        ),
        StudyResource(
            "Google Scholar",
            "Discover academic articles, journals and research papers.",
            "Research",
            "https://scholar.google.com/"
        ),
        StudyResource(
            "Wolfram MathWorld",
            "Mathematics explanations, definitions and examples.",
            "Notes",
            "https://mathworld.wolfram.com/"
        ),
        StudyResource(
            "W3Schools",
            "Learn programming and web development through tutorials.",
            "Notes",
            "https://www.w3schools.com/"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)

        searchInput = findViewById(R.id.etSearchResource)
        resourceContainer = findViewById(R.id.resourceListContainer)
        noResources = findViewById(R.id.tvNoResources)
        resourceCount = findViewById(R.id.tvResourceCount)
        savedSwitch = findViewById(R.id.switchSavedResources)

        savedResources.addAll(
            preferences.getStringSet("savedResourceTitles", emptySet())
                ?: emptySet()
        )

        setupCategoryButtons()

        savedSwitch.setOnCheckedChangeListener { _, checked ->
            showSavedOnly = checked
            displayResources()
        }

        searchInput.addTextChangedListener(object :
            android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                displayResources()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        displayResources()
    }

    private fun setupCategoryButtons() {

        findViewById<Button>(R.id.btnAllResources)
            .setOnClickListener {
                selectCategory("All", R.id.btnAllResources)
            }

        findViewById<Button>(R.id.btnNotes)
            .setOnClickListener {
                selectCategory("Notes", R.id.btnNotes)
            }

        findViewById<Button>(R.id.btnVideos)
            .setOnClickListener {
                selectCategory("Videos", R.id.btnVideos)
            }

        findViewById<Button>(R.id.btnBooks)
            .setOnClickListener {
                selectCategory("Textbooks", R.id.btnBooks)
            }

        findViewById<Button>(R.id.btnResearch)
            .setOnClickListener {
                selectCategory("Research", R.id.btnResearch)
            }
    }

    private fun selectCategory(category: String, selectedButtonId: Int) {
        selectedCategory = category

        val buttonIds = listOf(
            R.id.btnAllResources,
            R.id.btnNotes,
            R.id.btnVideos,
            R.id.btnBooks,
            R.id.btnResearch
        )

        buttonIds.forEach { id ->
            val button = findViewById<Button>(id)
            button.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    if (id == selectedButtonId) 0xFF075985.toInt()
                    else 0xFFFFFFFF.toInt()
                )

            button.setTextColor(
                if (id == selectedButtonId) 0xFFFFFFFF.toInt()
                else 0xFF075985.toInt()
            )
        }

        displayResources()
    }

    private fun displayResources() {

        resourceContainer.removeAllViews()

        val keyword = searchInput.text.toString().trim()

        val filtered = resources.filter { resource ->

            val matchesCategory =
                selectedCategory == "All" ||
                        resource.category == selectedCategory

            val matchesSearch =
                resource.title.contains(keyword, true) ||
                        resource.description.contains(keyword, true) ||
                        resource.category.contains(keyword, true)

            val matchesSaved =
                !showSavedOnly ||
                        savedResources.contains(resource.title)

            matchesCategory && matchesSearch && matchesSaved
        }

        resourceCount.text =
            "${filtered.size} learning resources available"

        noResources.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        noResources.text =
            if (showSavedOnly) "You have not saved any matching resources yet."
            else "No resources match your search."

        filtered.forEach { resource ->
            createResourceCard(resource)
        }
    }

    private fun createResourceCard(resource: StudyResource) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundResource(R.drawable.bg_resource_card)
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = when (resource.category) {
                "Textbooks" -> "📚"
                "Videos" -> "▶️"
                "Research" -> "🔎"
                "Courses" -> "🎓"
                else -> "📝"
            }
            textSize = 25f
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0xFFE0F2FE.toInt())
            setPadding(12, 10, 12, 10)
        }

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(12, 0, 4, 0)
        }

        val title = TextView(this).apply {
            text = resource.title
            textSize = 16f
            setTextColor(0xFF075985.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val category = TextView(this).apply {
            text = resource.category
            textSize = 11f
            setTextColor(0xFF0284C7.toInt())
            setPadding(0, 4, 0, 0)
        }

        titleColumn.addView(title)
        titleColumn.addView(category)

        val bookmarkButton = TextView(this).apply {
            text = if (savedResources.contains(resource.title)) "★" else "☆"
            textSize = 27f
            setTextColor(0xFFF59E0B.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(8, 4, 8, 4)

            contentDescription = "Save or remove favourite"

            setOnClickListener {
                toggleSaved(resource)
            }
        }

        topRow.addView(icon)
        topRow.addView(titleColumn)
        topRow.addView(bookmarkButton)

        val description = TextView(this).apply {
            text = resource.description
            textSize = 13f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 14, 0, 12)
        }

        val openButton = Button(this).apply {
            text = "Open Learning Resource  →"
            isAllCaps = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    0xFF075985.toInt()
                )

            setOnClickListener {
                openResource(resource.url)
            }
        }

        card.addView(topRow)
        card.addView(description)
        card.addView(openButton)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 14)

        resourceContainer.addView(card, params)
    }

    private fun toggleSaved(resource: StudyResource) {

        if (savedResources.contains(resource.title)) {
            savedResources.remove(resource.title)

            Toast.makeText(
                this,
                "Removed from saved resources",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            savedResources.add(resource.title)

            Toast.makeText(
                this,
                "Resource saved successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        preferences.edit()
            .putStringSet(
                "savedResourceTitles",
                savedResources.toSet()
            )
            .apply()

        displayResources()
    }

    private fun openResource(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open this resource.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
