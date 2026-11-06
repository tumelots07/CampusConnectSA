package com.example.campusconnectsa

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class CampusConnectApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = getSharedPreferences(
            "CampusConnectSettings",
            MODE_PRIVATE
        )

        val darkMode = preferences.getBoolean(
            "darkModeEnabled",
            false
        )

        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}