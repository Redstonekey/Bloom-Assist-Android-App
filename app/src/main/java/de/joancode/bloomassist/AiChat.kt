package de.joancode.bloomassist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import de.joancode.bloomassist.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiChat : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var addButton: FloatingActionButton
    private lateinit var chatTitle: TextView

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.aichat)
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout)
        recyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        addButton = findViewById(R.id.addButton)
        chatTitle = findViewById(R.id.chatTitle)

        // Set up drawer menu
        setupDrawerMenu()

        // Set up the RecyclerView
        setupRecyclerView()

        // Set up the "New Chat" button
        val newChatButton: ImageView = findViewById(R.id.newChatButton)
        newChatButton.setOnClickListener {
            showNewChatDialog()
        }

        // Set up the "Send" button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Set up the "Add" button
        addButton.setOnClickListener {
            showAddOptions()
        }

        // Add a welcome message
        addAIMessage("Hello! I'm your plant assistant. How can I help you today?")
    }

    private fun setupDrawerMenu() {
        // Set up menu icon click listener to open the drawer
        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up the navigation item selection listener
        val navigationView: NavigationView = findViewById(R.id.nav_view)
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
                    // Already in Chat activity
                }
                R.id.nav_user_settings -> {
                    val intent = Intent(this, user::class.java)
                    startActivity(intent)
                }
                R.id.nav_about -> {
                    // Handle About this Project action
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messageList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AiChat).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun showAddOptions() {
        val popupMenu = PopupMenu(this, addButton)
        popupMenu.menuInflater.inflate(R.menu.add_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_gallery -> {
                    openGallery()
                    true
                }
                R.id.option_select_plant -> {
                    showPlantSelectionDialog()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun showPlantSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_plant, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Get references to dialog views
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.plantRadioGroup)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val selectButton = dialogView.findViewById<Button>(R.id.selectButton)

        // Set up button click listeners
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        selectButton.setOnClickListener {
            val selectedPlantId = radioGroup.checkedRadioButtonId
            var plantName = ""

            when (selectedPlantId) {
                R.id.plantMonstera -> plantName = "Monstera Deliciosa"
                R.id.plantFicus -> plantName = "Fiddle Leaf Fig"
            }

            if (plantName.isNotEmpty()) {
                addUserMessageWithPlant(plantName)
                simulateAIResponse(plantName)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNewChatDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_chat, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Get references to dialog views
        val chatNameInput = dialogView.findViewById<EditText>(R.id.chatNameInput)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val createButton = dialogView.findViewById<Button>(R.id.createButton)

        // Set up button click listeners
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        createButton.setOnClickListener {
            val newChatName = chatNameInput.text.toString().trim()

            if (newChatName.isNotEmpty()) {
                // Update current chat name
                chatTitle.text = newChatName

                // Clear current messages
                messageList.clear()
                messageAdapter.notifyDataSetChanged()

                // Add welcome message to new chat
                addAIMessage("Hello! This is a new chat about $newChatName. How can I help you?")
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            addUserMessage(messageText)
            messageInput.text.clear()

            // Simulate AI response
            simulateAIResponse(messageText)
        }
    }

    private fun addUserMessage(text: String) {
        val currentTime = getCurrentTime()
        val message = Message(
            text = text,
            isUser = true,
            time = currentTime
        )
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        scrollToBottom()
    }

    private fun addUserMessageWithImage(text: String, imageUri: Uri) {
        val currentTime = getCurrentTime()
        val message = Message(
            text = text,
            isUser = true,
            time = currentTime,
            imageUri = imageUri
        )
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        scrollToBottom()
    }

    private fun addUserMessageWithPlant(plantName: String) {
        val currentTime = getCurrentTime()
        val message = Message(
            text = "I'd like to know more about my plant",
            isUser = true,
            time = currentTime,
            plantName = plantName
        )
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        scrollToBottom()
    }

    private fun addAIMessage(text: String) {
        val currentTime = getCurrentTime()
        val message = Message(
            text = text,
            isUser = false,
            time = currentTime
        )
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        scrollToBottom()
    }

    private fun simulateAIResponse(prompt: String) {
        // In a real app, this would call your AI service
        // For demo purposes, we'll just simulate responses

        val response = when {
            prompt.contains("Monstera", ignoreCase = true) ->
                "Monstera Deliciosa is a popular tropical plant known for its distinctive split leaves. " +
                        "It prefers bright, indirect light and should be watered when the top inch of soil is dry."

            prompt.contains("Fiddle Leaf Fig", ignoreCase = true) ->
                "Fiddle Leaf Fig (Ficus lyrata) has large, violin-shaped leaves. " +
                        "It needs bright, filtered light and consistent watering when the top 2 inches of soil dry out. " +
                        "It's sensitive to change, so try to maintain a consistent environment."

            prompt.contains("water", ignoreCase = true) ->
                "Proper watering is crucial for plant health. Most plants prefer to dry out slightly between waterings. " +
                        "Check the soil moisture by inserting your finger about an inch deep - if it feels dry, it's time to water."

            else -> "I'm here to help with your plant questions! Feel free to ask me about plant care, identification, or any other plant-related topics."
        }

        // Add a small delay to simulate processing time
        recyclerView.postDelayed({
            addAIMessage(response)
        }, 1000)
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data!!

            // Add the selected image to the chat
            addUserMessageWithImage("Here's a picture of my plant:", imageUri)

            // Simulate AI response
            val response = "Thanks for sharing the image of your plant! " +
                    "It looks healthy overall. Make sure to keep it in appropriate light conditions " +
                    "and maintain a consistent watering schedule based on its specific needs."

            recyclerView.postDelayed({
                addAIMessage(response)
            }, 1500)
        }
    }
}