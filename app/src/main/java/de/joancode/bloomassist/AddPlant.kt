package de.joancode.bloomassist

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddPlantActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingLayout: RelativeLayout
    private lateinit var bottomRightCorner: ImageView
    private lateinit var bottomLeftCorner: ImageView
    private lateinit var topRightCorner: ImageView
    private lateinit var topLeftCorner: ImageView
    private lateinit var previewContainer: CardView



    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val API_KEY = "2b10K63MkVEbJpGgJBqgTrnhT"
    private val PROJECT = "all"
    private val API_ENDPOINT = "https://api.plantnet.org/v1/identify/$PROJECT?api-key=$API_KEY"
    private lateinit var scanLine: View
    private lateinit var scanAnimation: ObjectAnimator
    private lateinit var container: FrameLayout  // A container to load the loading layout




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_plant)
        captureButton = findViewById(R.id.captureButton) // Stelle sicher, dass die ID stimmt
        scanLine = findViewById(R.id.scanLine)

        container = findViewById(R.id.container)  // Make sure you have a container in your layout

        val btnback = findViewById<ImageButton>(R.id.btn_back)
        btnback.setOnClickListener { finish() }

        // Animation erstellen
        scanAnimation = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, 400f).apply {
            duration = 1500 // 2 Sekunden für eine Bewegung
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        captureButton.setOnClickListener {
            scanAnimation.cancel() // Scan-Animation stoppen
            scanLine.visibility = View.GONE // Scan-Linie verstecken
            progressBar.visibility = View.VISIBLE // Lade-Icon anzeigen
            takePicture()
        }

        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        progressBar = findViewById(R.id.progressBar)
        bottomRightCorner = findViewById(R.id.bottomRightCorner)

        bottomLeftCorner = findViewById(R.id.bottomLeftCorner)

        topRightCorner = findViewById(R.id.topRightCorner)

        topLeftCorner = findViewById(R.id.topLeftCorner)

        previewContainer = findViewById(R.id.previewContainer)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions immediately when the activity starts
        requestCameraPermissions()

        captureButton.setOnClickListener {
            takePicture()
            showLoadingLayout()
        }

    }
    private fun showLoadingLayout() {
        // Hide all relevant views
        previewView.visibility = View.GONE
        captureButton.visibility = View.GONE
        scanLine.visibility = View.GONE
        bottomRightCorner.visibility = View.GONE
        bottomLeftCorner.visibility = View.GONE
        topLeftCorner.visibility = View.GONE
        topRightCorner.visibility = View.GONE
        previewContainer.visibility = View.GONE

        // Inflate and show the loading layout
        val loadingView = layoutInflater.inflate(R.layout.loading, container, false)
        container.removeAllViews()  // Remove any existing views in the container
        container.addView(loadingView)  // Add the loading layout
    }




    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCameraPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (allPermissionsGranted(permissions)) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun allPermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun takePicture() {
        val imageCapture = imageCapture ?: return

        captureButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    val file = createTempImageFile()
                    saveBitmapToFile(bitmap, file)

                    // Send to API
                    identifyPlant(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    captureButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        // Create bitmap and rotate if needed
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Rotate the bitmap based on the image's orientation
        val matrix = Matrix()
        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun createTempImageFile(): File {
        val fileName = "plant_image_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        return file
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun identifyPlant(imageFile: File) {
        Thread {
            try {
                val client = OkHttpClient()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "images",
                        imageFile.name,
                        RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
                    )
                    .addFormDataPart("organs", "auto") // Or "leaf", "flower", etc. depending on what you are taking picture of.
                    .build()

                val apiUrl = "https://my-api.plantnet.org/v2/identify/all?include-related-images=false&no-reject=false&nb-results=10&lang=de-at&api-key=2b10K63MkVEbJpGgJBqgTrnhT"

                Log.d(TAG, "Full API URL: $apiUrl")

                val request = Request.Builder()
                    .url(apiUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    runOnUiThread {
                        captureButton.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (response.isSuccessful && responseBody != null) {
                            handleApiResponse(responseBody)
                        } else {
                            Log.e(TAG, "API Error Code: ${response.code}")
                            Log.e(TAG, "API Error Body: $responseBody")
                            Toast.makeText(
                                baseContext,
                                "API Error: ${response.code}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "API request failed", e)
                runOnUiThread {
                    captureButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext,
                        "API request failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun handleApiResponse(responseBody: String) {
        try {
            val json = JSONObject(responseBody)
            if (json.has("results") && json.getJSONArray("results").length() > 0) {
                val result = json.getJSONArray("results").getJSONObject(0)
                val species = result.getJSONObject("species")
                val scientificName = species.getString("scientificName")

                // Show plant details form
                showPlantDetailsForm(scientificName)
            } else {
                Toast.makeText(baseContext, "No plant identified", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API response", e)
            Toast.makeText(baseContext, "Error parsing API response", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPlantDetailsForm(scientificName: String) {
        // Replace camera preview with form
        container.removeAllViews()
        val formView = layoutInflater.inflate(R.layout.plant_details_form, container, false)
        container.addView(formView)

        // Pre-fill the name field
        val nameInput = formView.findViewById<TextInputEditText>(R.id.nameInput)
        nameInput.setText(scientificName)

        // Set current date
        val dateInput = formView.findViewById<TextInputEditText>(R.id.dateInput)
        dateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        // Handle form submission
        formView.findViewById<Button>(R.id.submitButton).setOnClickListener {
            submitPlantDetails(formView)
        }
    }

    private fun submitPlantDetails(formView: View) {
        val name = formView.findViewById<TextInputEditText>(R.id.nameInput).text.toString()
        val type = formView.findViewById<TextInputEditText>(R.id.typeInput).text.toString()
        val location = formView.findViewById<TextInputEditText>(R.id.locationInput).text.toString()
        val date = formView.findViewById<TextInputEditText>(R.id.dateInput).text.toString()
        val notes = formView.findViewById<TextInputEditText>(R.id.notesInput).text.toString()

        // Create JSON body
        val jsonBody = JSONObject().apply {
            put("name", name)
            put("plant_type", type)
            put("plant_location", location)
            put("plant_date", date)
            put("notes", notes)
        }

        // Get token
        val token = Token(this).getToken()
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Send to API
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${ApiService.getBaseUrl()}/api/add-plant")
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString()))
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    runOnUiThread {
                        if (response.isSuccessful && responseBody != null) {
                            val json = JSONObject(responseBody)
                            if (json.getBoolean("success")) {
                                Toast.makeText(this, "Plant added successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Error adding plant", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "AddPlantActivity"
    }
}