package de.joancode.bloomassist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var userIcon: ImageView
    private lateinit var btnPlantDetails: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btn_add_plant: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Set up the navigation item selection listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_add_plant -> {
                    // Handle Add a Plant action
                }
                R.id.nav_chat -> {
                    // Handle Chat with AI action
                }
                R.id.nav_settings -> {
                    // Handle Settings action
                }
                R.id.nav_about -> {
                    // Handle About this Project action
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after selection
            true
        }

        // Set up user icon click listener
        userIcon = findViewById(R.id.userIcon)
        userIcon.setOnClickListener {
            val intent = Intent(this, user::class.java)
            startActivity(intent)
        }

        // Set up plant details button click listener
//        btnPlantDetails = findViewById(R.id.btn_plant_details)
//        btnPlantDetails.setOnClickListener {
//            val intent = Intent(this, plant_details::class.java)
//            startActivity(intent)
//        }

        // Set up menu icon click listener to open the drawer
        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        btn_add_plant = findViewById(R.id.fab_add)
        btn_add_plant.setOnClickListener {
            val intent = Intent(this, AddPlantActivity::class.java)
            startActivity(intent)
        }
        val alertButton = findViewById<Button>(R.id.btn_plant_details)
        AlertHandler.createNotificationChannel(this)
        alertButton.setOnClickListener {
            AlertHandler.sendNotification(this)
        }
    }

}
