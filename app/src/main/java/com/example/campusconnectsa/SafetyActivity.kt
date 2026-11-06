package com.example.campusconnectsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SafetyActivity : AppCompatActivity() {

    private lateinit var incidentType: Spinner
    private lateinit var incidentLocation: EditText
    private lateinit var incidentDescription: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety)

        incidentType = findViewById(R.id.spIncidentType)
        incidentLocation = findViewById(R.id.etIncidentLocation)
        incidentDescription = findViewById(R.id.etIncidentDescription)
        submitButton = findViewById(R.id.btnSubmitIncident)

        setupIncidentTypes()
        setupEmergencyButtons()

        submitButton.setOnClickListener {
            submitIncidentReport()
        }
    }

    private fun setupIncidentTypes() {
        val incidentTypes = listOf(
            "Select incident type",
            "Theft",
            "Harassment",
            "Medical emergency",
            "Suspicious activity",
            "Accident",
            "Personal safety concern",
            "Other"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            incidentTypes
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        incidentType.adapter = adapter
    }

    private fun setupEmergencyButtons() {
        findViewById<Button>(R.id.btnEmergencyCall).setOnClickListener {
            openDialler("112")
        }

        findViewById<Button>(R.id.btnPoliceCall).setOnClickListener {
            openDialler("10111")
        }

        findViewById<Button>(R.id.btnAmbulanceCall).setOnClickListener {
            openDialler("10177")
        }
    }

    private fun openDialler(number: String) {
        try {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$number")
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open the phone dialler",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun submitIncidentReport() {
        val selectedType = incidentType.selectedItem.toString()
        val location = incidentLocation.text.toString().trim()
        val description = incidentDescription.text.toString().trim()

        when {
            selectedType == "Select incident type" -> {
                incidentType.requestFocus()
                Toast.makeText(
                    this,
                    "Please select an incident type",
                    Toast.LENGTH_SHORT
                ).show()
            }

            location.isEmpty() -> {
                incidentLocation.error = "Please enter the incident location"
                incidentLocation.requestFocus()
            }

            description.isEmpty() -> {
                incidentDescription.error = "Please describe the incident"
                incidentDescription.requestFocus()
            }

            description.length < 10 -> {
                incidentDescription.error =
                    "Please provide at least 10 characters"
                incidentDescription.requestFocus()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Incident report captured for demonstration. It has not been sent to campus security.",
                    Toast.LENGTH_LONG
                ).show()

                incidentType.setSelection(0)
                incidentLocation.text.clear()
                incidentDescription.text.clear()
            }
        }
    }
}