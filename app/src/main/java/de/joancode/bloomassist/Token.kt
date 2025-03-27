package de.joancode.bloomassist

import android.content.Context
import android.content.SharedPreferences

class Token(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        preferences.edit().putString("auth_token", token).apply()
        android.util.Log.d("Token", "Token saved: $token")
    }

    fun getToken(): String? {
        return preferences.getString("auth_token", null)
    }

    fun clearToken() {
        preferences.edit().remove("auth_token").apply()
    }
}