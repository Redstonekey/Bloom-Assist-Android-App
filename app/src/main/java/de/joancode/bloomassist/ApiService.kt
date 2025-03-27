package de.joancode.bloomassist

import okhttp3.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

object ApiService {
    private var baseUrl: String = ""
    private val configFetcher = GetConfig()
    private var isInitialized = false

    init {
        configFetcher.fetchConfig { config ->
            if (config != null) {
                baseUrl = config.authApiUrl.trim()
                isInitialized = true
                println("Fetched API URL: $baseUrl")
            } else {
                println("Failed to fetch config")
            }
        }
    }

    private val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }).build()

    fun deletePlant(token: String, plantId: String, callback: (Boolean, String) -> Unit) {
        if (!isInitialized || baseUrl.isEmpty()) {
            callback(false, "API not initialized")
            return
        }

        val fullUrl = if (!baseUrl.startsWith("http")) {
            "http://$baseUrl/api/delete/$plantId"
        } else {
            "$baseUrl/api/delete/$plantId"
        }

        val request = Request.Builder()
            .url(fullUrl)
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val responseBody = it.body?.string()
                        val jsonResponse = Gson().fromJson(responseBody, JsonObject::class.java)
                        val message = jsonResponse.get("message").asString
                        callback(false, message)
                        return
                    }

                    val responseBody = it.body?.string()
                    val jsonResponse = Gson().fromJson(responseBody, JsonObject::class.java)
                    val success = jsonResponse.get("success").asBoolean
                    val message = jsonResponse.get("message").asString

                    callback(success, message)
                }
            }
        })
    }
}