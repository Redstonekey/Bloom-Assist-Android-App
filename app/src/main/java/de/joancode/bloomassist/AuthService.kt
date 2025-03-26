// AuthService.kt
package de.joancode.bloomassist

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthService(private val baseUrl: String) {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    data class AuthResponse(
        val success: Boolean,
        val message: String,
        val token: String? = null,
        val email: String? = null
    )

    interface AuthCallback {
        fun onSuccess(response: AuthResponse)
        fun onError(message: String)
    }

    fun login(email: String, password: String, callback: AuthCallback) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url("$baseUrl/api/login")
            .post(json.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                val authResponse = AuthResponse(
                    success = jsonResponse.getBoolean("success"),
                    message = jsonResponse.getString("message"),
                    token = if (jsonResponse.has("token")) jsonResponse.getString("token") else null,
                    email = if (jsonResponse.has("email")) jsonResponse.getString("email") else null
                )

                callback.onSuccess(authResponse)
            }
        })
    }

    fun signup(email: String, password: String, callback: AuthCallback) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url("$baseUrl/api/signup")
            .post(json.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                val authResponse = AuthResponse(
                    success = jsonResponse.getBoolean("success"),
                    message = jsonResponse.getString("message"),
                    token = if (jsonResponse.has("token")) jsonResponse.getString("token") else null,
                    email = if (jsonResponse.has("email")) jsonResponse.getString("email") else null
                )

                callback.onSuccess(authResponse)
            }
        })
    }
}