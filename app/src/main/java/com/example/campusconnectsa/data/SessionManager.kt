
package com.example.campusconnectsa.data

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "CampusConnectSession",
        Context.MODE_PRIVATE
    )

    fun saveSession(token: String, user: UserData) {
        preferences.edit()
            .putString("token", token)
            .putString("userId", user.id)
            .putString("fullName", user.fullName)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString("token", null)
    }

    fun getFullName(): String? {
        return preferences.getString("fullName", null)
    }

    fun getEmail(): String? {
        return preferences.getString("email", null)
    }

    fun getRole(): String? {
        return preferences.getString("role", null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}
