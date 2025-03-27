package de.joancode.bloomassist

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout

class PlantDetails : AppCompatActivity() {
    private lateinit var token: String
    private var plantId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plant_details)

        // Get plant ID from intent
        plantId = intent.getStringExtra("plantId")
        token = Token(this).getToken() ?: ""

        // Set up back button
        findViewById<LinearLayout>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Set up action buttons
        setupActionButtons()

        // Load plant details
        loadPlantDetails()
    }

    private fun loadPlantDetails() {
        if (plantId == null) {
            finish()
            return
        }

        // Show loading states for all UI elements
        findViewById<TextView>(R.id.plantName).text = "Loading..."
        findViewById<TextView>(R.id.moistureValue).text = "..."
        findViewById<TextView>(R.id.moistureStatus).text = "Loading..."
        findViewById<TextView>(R.id.wateringTip).text = "Loading..."
        findViewById<TextView>(R.id.lightTip).text = "Loading..."
        findViewById<TextView>(R.id.fertilizerTip).text = "Loading..."

        // Set progress to 0 while loading
        findViewById<android.widget.ProgressBar>(R.id.moistureProgress).progress = 0

        try {
            ApiService.getPlants(token) { success, plants, message ->
                runOnUiThread {
                    if (success && plants != null) {
                        val plant = plants.find { it.id.toString() == plantId }
                        if (plant != null) {
                            updateUI(plant)
                        } else {
                            showError("Plant not found")
                        }
                    } else {
                        showError(message)
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                showError("Failed to load plant details: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }



    private fun updateUI(plant: Plant) {
        // Update plant name
        findViewById<TextView>(R.id.plantName).text = plant.name

        // Update moisture value and progress
        val moistureValue = findViewById<TextView>(R.id.moistureValue)
        val moistureProgress = findViewById<android.widget.ProgressBar>(R.id.moistureProgress)
        val moistureStatus = findViewById<TextView>(R.id.moistureStatus)

        val moisturePercentage = plant.moisture.replace("%", "").toIntOrNull() ?: 0
        moistureValue.text = "${plant.moisture}"
        moistureProgress.progress = moisturePercentage
        moistureStatus.text = when {
            moisturePercentage < 30 -> "Too dry"
            moisturePercentage > 70 -> "Too wet"
            else -> "Optimal"
        }

        // Update care tips
        findViewById<TextView>(R.id.wateringTip).text = plant.wateringTip
        findViewById<TextView>(R.id.lightTip).text = plant.lightTip
        findViewById<TextView>(R.id.fertilizerTip).text = plant.fertilizerTip
    }

    private fun setupActionButtons() {
        // Edit button
        findViewById<Button>(R.id.editButton).setOnClickListener {
            val intent = Intent(this, AddPlantActivity::class.java).apply {
                putExtra("plantId", plantId)
                putExtra("mode", "edit")
            }
            startActivity(intent)
        }

        // Chat button
        findViewById<Button>(R.id.chatButton).setOnClickListener {
            val intent = Intent(this, AiChat::class.java)
            startActivity(intent)
        }

        // Delete button
        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Plant")
            .setMessage("Are you sure you want to delete this plant?")
            .setPositiveButton("Delete") { _, _ ->
                deletePlant()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlant() {
        plantId?.let { id ->
            ApiService.deletePlant(token, id) { success, message ->
                runOnUiThread {
                    if (success) {
                        finish()
                    } else {
                        // Show error message
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }
}