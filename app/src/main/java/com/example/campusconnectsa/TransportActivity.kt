package com.example.campusconnectsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class TransportOption(
    val name: String,
    val route: String,
    val type: String,
    val departure: String,
    val fare: String,
    val contact: String,
    val website: String
)

class TransportActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var routeContainer: LinearLayout
    private lateinit var noTransportText: TextView
    private lateinit var countText: TextView
    private lateinit var savedSwitch: Switch

    private val preferences by lazy {
        getSharedPreferences("CampusConnectTransport", MODE_PRIVATE)
    }

    private val routes = listOf(
        TransportOption(
            "Campus Shuttle",
            "Student residences → Main campus",
            "Shuttle",
            "07:00, 08:00, 09:00",
            "Free (sample)",
            "Contact campus transport office",
            "https://www.google.com/maps"
        ),
        TransportOption(
            "Public Bus",
            "Town centre → Main campus",
            "Bus",
            "06:30, 07:30, 08:30",
            "R15 (sample)",
            "Contact local bus operator",
            "https://www.google.com/maps"
        ),
        TransportOption(
            "Local Taxi",
            "Town taxi rank → Main campus",
            "Taxi",
            "Available throughout the day",
            "R20 (sample)",
            "Contact local taxi association",
            "https://www.google.com/maps"
        ),
        TransportOption(
            "Intercity Bus",
            "Nearby towns → Campus area",
            "Intercity",
            "Check provider timetable",
            "From R80 (sample)",
            "Contact bus provider",
            "https://www.google.com/maps"
        )
    )

    private var selectedType = "All"
    private var showSavedOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)

        searchInput = findViewById(R.id.etSearchTransport)
        typeSpinner = findViewById(R.id.spTransportType)
        routeContainer = findViewById(R.id.transportListContainer)
        noTransportText = findViewById(R.id.tvNoTransport)
        countText = findViewById(R.id.tvTransportCount)
        savedSwitch = findViewById(R.id.switchSavedRoutes)

        setupTransportFilters()
        displayRoutes()
    }

    private fun setupTransportFilters() {
        val types = listOf("All", "Shuttle", "Bus", "Taxi", "Intercity")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            types
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        typeSpinner.adapter = adapter

        typeSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedType = types[position]
                    displayRoutes()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                displayRoutes()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        savedSwitch.setOnCheckedChangeListener { _, isChecked ->
            showSavedOnly = isChecked
            displayRoutes()
        }
    }

    private fun getSavedRoutes(): MutableSet<String> {
        return preferences.getStringSet(
            "savedRoutes",
            emptySet()
        )?.toMutableSet() ?: mutableSetOf()
    }

    private fun toggleFavourite(routeName: String) {
        val saved = getSavedRoutes()

        if (saved.contains(routeName)) {
            saved.remove(routeName)
            Toast.makeText(this, "Route removed from favourites", Toast.LENGTH_SHORT).show()
        } else {
            saved.add(routeName)
            Toast.makeText(this, "Route added to favourites", Toast.LENGTH_SHORT).show()
        }

        preferences.edit()
            .putStringSet("savedRoutes", saved)
            .apply()

        displayRoutes()
    }

    private fun displayRoutes() {
        routeContainer.removeAllViews()

        val searchText = searchInput.text.toString().trim().lowercase()
        val savedRoutes = getSavedRoutes()

        val filteredRoutes = routes.filter { route ->

            val matchesSearch =
                route.name.lowercase().contains(searchText) ||
                        route.route.lowercase().contains(searchText) ||
                        route.type.lowercase().contains(searchText)

            val matchesType =
                selectedType == "All" || route.type == selectedType

            val matchesSaved =
                !showSavedOnly || savedRoutes.contains(route.name)

            matchesSearch && matchesType && matchesSaved
        }

        countText.text = "${filteredRoutes.size} routes"
        noTransportText.visibility =
            if (filteredRoutes.isEmpty()) View.VISIBLE else View.GONE

        for (route in filteredRoutes) {
            createRouteCard(route, savedRoutes.contains(route.name))
        }
    }

    private fun createRouteCard(
        route: TransportOption,
        isSaved: Boolean
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = getDrawable(R.drawable.bg_transport_card)
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = 14
        card.layoutParams = cardParams

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = when (route.type) {
                "Shuttle" -> "🚌"
                "Bus" -> "🚍"
                "Taxi" -> "🚕"
                else -> "🚐"
            }
            textSize = 25f
        }

        val title = TextView(this).apply {
            text = route.name
            textSize = 17f
            setTextColor(android.graphics.Color.rgb(15, 41, 66))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val titleParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        titleParams.marginStart = 12
        topRow.addView(icon)
        topRow.addView(title, titleParams)

        val favouriteButton = Button(this).apply {
            text = if (isSaved) "★" else "☆"
            textSize = 20f
            setTextColor(android.graphics.Color.rgb(2, 132, 199))
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            minWidth = 42
            setPadding(4, 0, 4, 0)
            setOnClickListener {
                toggleFavourite(route.name)
            }
        }

        topRow.addView(favouriteButton)
        card.addView(topRow)

        addDetail(card, "ROUTE", route.route)
        addDetail(card, "TRANSPORT", route.type)
        addDetail(card, "DEPARTURE", route.departure)
        addDetail(card, "ESTIMATED FARE", route.fare)
        addDetail(card, "CONTACT", route.contact)

        val mapButton = Button(this).apply {
            text = "View route / provider"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(2, 132, 199)
                )
            )
            setOnClickListener {
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(route.website))
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@TransportActivity,
                        "Unable to open the link",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.topMargin = 12
        card.addView(mapButton, buttonParams)

        routeContainer.addView(card)
    }

    private fun addDetail(
        card: LinearLayout,
        label: String,
        value: String
    ) {
        val detail = TextView(this).apply {
            text = "$label: $value"
            textSize = 13f
            setTextColor(android.graphics.Color.rgb(100, 116, 139))
            setPadding(0, 8, 0, 0)
        }
        card.addView(detail)
    }
}