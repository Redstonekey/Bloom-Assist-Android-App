package de.joancode.bloomassist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class PlantDetails: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.plant_details)
        val tokenManager = Token(this)
        val token = tokenManager.getToken()
        val plantId = "1"
        val btnback = findViewById<LinearLayout>(R.id.backButton)
        btnback.setOnClickListener { finish() }
        val btn_ai =findViewById<Button>(R.id.chatButton)
        btn_ai.setOnClickListener{
            val intent = Intent(this, AiChat::class.java)
            startActivity(intent)

        }
        val btn_delete = findViewById<Button>(R.id.deleteButton)
        btn_delete.setOnClickListener{
            ApiService.deletePlant(token ?: "", plantId) { success, message ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Plant deleted successfully: $message", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete plant: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

    }
}
