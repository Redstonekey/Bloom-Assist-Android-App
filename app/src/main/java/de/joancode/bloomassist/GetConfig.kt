package de.joancode.bloomassist

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GetConfig {
    private val client = OkHttpClient()

    private val gistUrl = "https://gist.githubusercontent.com/Redstonekey/01b6ba6791f150cceb5cc00485bcefdd/raw/config.json?nocache=" + System.currentTimeMillis()

    fun interface ConfigCallback {
        fun onResult(config: Config?)
    }

    data class Config(
        val authApiUrl: String,
        val authApiVersion: String = "0"
    )

    fun fetchConfig(callback: ConfigCallback) {
        val request = Request.Builder().url(gistUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback.onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onResult(null)
                    return
                }

                response.body?.let {
                    try {
                        val json = JSONObject(it.string())
                        val config = Config(
                            authApiUrl = json.getString("auth_api_url"),
                            authApiVersion = json.optString("auth_api_url_v", "0")
                        )
                        callback.onResult(config)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onResult(null)
                    }
                } ?: callback.onResult(null)
            }
        })
    }
}
//  java:
//        configFetcher.fetchConfig(config -> {
//            if (config != null) {
//                String apiUrl = config.getAuthApiUrl();
//                // Use the apiUrl here
//                System.out.println("Fetched API URL: " + apiUrl);
//            } else {
//                // Handle null config (error case)
//                System.out.println("Failed to fetch config");
//            }
//        });


//kotlin:
//
//  val configFetcher = GetConfig()
//  configFetcher.fetchConfig { config ->
//      if (config != null) {
//          val apiUrl = config.authApiUrl
//          println("Fetched API URL: $apiUrl")
//      } else {
//          println("Failed to fetch config")
//      }
//}


