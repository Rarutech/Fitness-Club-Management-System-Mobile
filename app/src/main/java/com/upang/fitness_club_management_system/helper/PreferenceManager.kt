package com.upang.fitness_club_management_system.helper

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs : SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("AUTH_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("AUTH_TOKEN", null)
    }

    fun clearToken() {
        prefs.edit().remove("AUTH_TOKEN").apply()
    }

    fun saveEmail(email: String) {
        prefs.edit().putString("CURRENT_EMAIL", email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString("CURRENT_EMAIL", null)
    }

    fun clearEmail() {
        prefs.edit().remove("CURRENT_EMAIL").apply()
    }
}