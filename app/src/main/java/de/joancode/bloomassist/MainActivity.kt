package de.joancode.bloomassist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var userIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var plantContainer: LinearLayout
    private lateinit var btnAddPlant: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
        val token = Token(this).getToken()
        if (token == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        plantContainer = findViewById(R.id.plantContainer)
        btnAddPlant = findViewById(R.id.fab_add)
        if (token != null) {
            ApiService.getPlants(token) { success, plants, message ->
                runOnUiThread {
                    if (success && plants != null) {
                        displayPlants(plants)
                    } else {
                        // Handle error
                    }
                }
            }

            // Set up navigation
            setupNavigation(navigationView)

            // Set up user icon click listener
            userIcon = findViewById(R.id.userIcon)
            userIcon.setOnClickListener {
                val intent = Intent(this, user::class.java)
                startActivity(intent)
            }

            // Set up menu icon click listener
            val menuIcon: ImageView = findViewById(R.id.menuIcon)
            menuIcon.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            btnAddPlant.setOnClickListener {
                val intent = Intent(this, AddPlantActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupNavigation(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_add_plant -> {
                    val intent = Intent(this, AddPlantActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_chat -> {
                    val intent = Intent(this, AiChat::class.java)
                    startActivity(intent)
                }
                R.id.nav_user_settings -> {
                    val intent = Intent(this, user::class.java)
                    startActivity(intent)
                }
                R.id.nav_about -> {
                    AlertHandler.createNotificationChannel(this)
                    AlertHandler.sendNotification(this)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun displayPlants(plants: List<Plant>) {
        plantContainer.removeAllViews()
        for (plant in plants) {
            val plantView = layoutInflater.inflate(R.layout.plant_item, plantContainer, false)
            val plantName = plantView.findViewById<TextView>(R.id.plantName)
            val plantMoisture = plantView.findViewById<TextView>(R.id.plantMoisture)

            plantName.text = plant.name
            plantMoisture.text = "Feuchtigkeit: ${plant.moisture}"

            // Make the entire plant item clickable
            plantView.setOnClickListener {
                val intent = Intent(this, PlantDetails::class.java).apply {
                    putExtra("plantId", plant.id.toString())
                }
                startActivity(intent)
            }

            plantContainer.addView(plantView)
        }
    }
}