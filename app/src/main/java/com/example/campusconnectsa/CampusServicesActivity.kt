
package com.example.campusconnectsa

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class CampusService(
    val name: String,
    val description: String,
    val category: String,
    val location: String,
    val contact: String,
    val website: String,
    val icon: String
)

class CampusServicesActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var serviceContainer: LinearLayout
    private lateinit var noServices: TextView
    private lateinit var serviceCount: TextView

    private var selectedCategory = "All"

    private val services = listOf(
        CampusService(
            "Academic Support",
            "Get guidance with academic difficulties, study skills and learning support.",
            "Academic",
            "Academic Support Centre",
            "Ask your institution",
            "https://www.dhet.gov.za/",
            "🎓"
        ),
        CampusService(
            "Student Counselling",
            "Find emotional support, guidance and student wellbeing resources.",
            "Student Support",
            "Student Support Office",
            "Ask your institution",
            "https://www.sadag.org/",
            "🤝"
        ),
        CampusService(
            "Campus Library",
            "Discover books, study spaces, research assistance and library resources.",
            "Library",
            "Campus Library",
            "Ask your institution",
            "https://www.nlsa.ac.za/",
            "📚"
        ),
        CampusService(
            "Financial Aid",
            "Explore financial assistance and student funding information.",
            "Financial Aid",
            "Financial Aid Office",
            "Ask your institution",
            "https://www.nsfas.org.za/",
            "💰"
        ),
        CampusService(
            "Computer Laboratories",
            "Find information about computer facilities and technology support.",
            "Computer Labs",
            "Campus Computer Laboratory",
            "Ask your institution",
            "https://www.dhet.gov.za/",
            "💻"
        ),
        CampusService(
            "Student Administration",
            "Get help with registration, student records and administrative enquiries.",
            "Student Support",
            "Administration Office",
            "Ask your institution",
            "https://www.dhet.gov.za/",
            "🏢"
        ),
        CampusService(
            "Career Guidance",
            "Explore career development and employment preparation resources.",
            "Student Support",
            "Career Guidance Office",
            "Ask your institution",
            "https://www.careerhelp.org.za/",
            "🚀"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campus_services)

        searchInput = findViewById(R.id.etSearchService)
        serviceContainer = findViewById(R.id.serviceListContainer)
        noServices = findViewById(R.id.tvNoServices)
        serviceCount = findViewById(R.id.tvServiceCount)

        setupCategoryButtons()

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                displayServices()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        displayServices()
    }

    private fun setupCategoryButtons() {
        val buttons = mapOf(
            R.id.btnAllServices to "All",
            R.id.btnAcademicServices to "Academic",
            R.id.btnStudentServices to "Student Support",
            R.id.btnLibraryServices to "Library",
            R.id.btnFinancialServices to "Financial Aid",
            R.id.btnComputerServices to "Computer Labs"
        )

        buttons.forEach { (buttonId, category) ->
            findViewById<Button>(buttonId).setOnClickListener {
                selectedCategory = category
                updateCategoryButtons(buttons)
                displayServices()
            }
        }
    }

    private fun updateCategoryButtons(buttons: Map<Int, String>) {
        buttons.forEach { (id, category) ->
            val button = findViewById<Button>(id)
            val selected = category == selectedCategory

            button.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    if (selected) 0xFF075985.toInt()
                    else 0xFFFFFFFF.toInt()
                )

            button.setTextColor(
                if (selected) 0xFFFFFFFF.toInt()
                else 0xFF075985.toInt()
            )
        }
    }

    private fun displayServices() {
        serviceContainer.removeAllViews()

        val keyword = searchInput.text.toString().trim()

        val filtered = services.filter { service ->

            val matchesCategory =
                selectedCategory == "All" ||
                        service.category == selectedCategory

            val matchesSearch =
                service.name.contains(keyword, true) ||
                        service.description.contains(keyword, true) ||
                        service.location.contains(keyword, true) ||
                        service.category.contains(keyword, true)

            matchesCategory && matchesSearch
        }

        serviceCount.text = "${filtered.size} services"

        noServices.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        filtered.forEach { service ->
            createServiceCard(service)
        }
    }

    private fun createServiceCard(service: CampusService) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 17, 18, 17)
            setBackgroundResource(R.drawable.bg_service_card)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = service.icon
            textSize = 25f
            gravity = Gravity.CENTER
            setPadding(12, 11, 12, 11)
            setBackgroundColor(0xFFE0F2FE.toInt())
        }

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(12, 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = service.name
            textSize = 16f
            setTextColor(0xFF075985.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val category = TextView(this).apply {
            text = service.category
            textSize = 11f
            setTextColor(0xFF0284C7.toInt())
            setPadding(0, 4, 0, 0)
        }

        titleColumn.addView(title)
        titleColumn.addView(category)

        header.addView(icon)
        header.addView(titleColumn)

        val description = TextView(this).apply {
            text = service.description
            textSize = 13f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 14, 0, 12)
        }

        val location = TextView(this).apply {
            text = "📍 ${service.location}"
            textSize = 12f
            setTextColor(0xFF475569.toInt())
        }

        val contact = TextView(this).apply {
            text = "Contact: ${service.contact}"
            textSize = 12f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 7, 0, 10)
        }

        val visitButton = Button(this).apply {
            text = "Explore Service  →"
            isAllCaps = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    0xFF075985.toInt()
                )

            setOnClickListener {
                openWebsite(service.website)
            }
        }

        card.addView(header)
        card.addView(description)
        card.addView(location)
        card.addView(contact)
        card.addView(visitButton)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        serviceContainer.addView(card, params)
    }

    private fun openWebsite(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open the service website.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
