/**
 * Course code and lab number: COMP 301 - Lab 5
 * Your full name and Student ID: Ramandeep Singh - A00194321
 * Date of Submission: April 17, 2026
 *
 * Smart Utility App - A comprehensive Android utility app that demonstrates:
 * - Async data fetching using Coroutines with loading states
 * - Camera permission handling for image capture
 * - Robust error handling for network, permissions, and input validation
 * - Secure data handling with SharedPreferences encryption
 * - Navigation between Main screen and Results screen
 * - Clean UI using ConstraintLayout
 */

package com.example.smartutilityapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fetchDataButton: Button
    private lateinit var cameraButton: Button
    private lateinit var userInputEditText: EditText
    private lateinit var resultTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var statusTextView: TextView

    // Constants for permissions and requests
    private val CAMERA_PERMISSION_REQUEST = 1001
    private val CAMERA_CAPTURE_REQUEST = 1002

    // Secure storage helper
    private lateinit var secureStorage: SecureStorageHelper

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        secureStorage = SecureStorageHelper(this)
        setupClickListeners()
    }

    /**
     * Initializes all UI views and sets initial state
     */
    private fun initViews() {
        fetchDataButton = findViewById(R.id.fetchDataButton)
        cameraButton = findViewById(R.id.cameraButton)
        userInputEditText = findViewById(R.id.userInputEditText)
        resultTextView = findViewById(R.id.resultTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        statusTextView = findViewById(R.id.statusTextView)

        // Hide loading initially
        loadingProgressBar.visibility = ProgressBar.GONE
    }

    /**
     * Sets up click listeners for all interactive elements
     */
    private fun setupClickListeners() {
        fetchDataButton.setOnClickListener {
            val input = userInputEditText.text.toString().trim()
            if (validateInput(input)) {
                fetchDataAsync(input)
            } else {
                showError("Please enter a valid city name (3+ characters)")
            }
        }

        cameraButton.setOnClickListener {
            requestCameraPermission()
        }

        // Navigate to results screen (simulating navigation requirement)
        findViewById<Button>(R.id.viewResultsButton).setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Validates user input with security best practices
     * @param input User provided text
     * @return true if input is valid, false otherwise
     */
    private fun validateInput(input: String): Boolean {
        return input.isNotEmpty() && input.length >= 3 && input.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    /**
     * Fetches weather data asynchronously using Coroutines
     * Handles network failures gracefully
     * @param cityName City to fetch weather for
     */
    private fun fetchDataAsync(cityName: String) {
        // Show loading state
        showLoading(true)

        lifecycleScope.launch {
            try {
                val weatherData = withContext(Dispatchers.IO) {
                    fetchWeatherData(cityName)
                }

                // Securely store the result
                secureStorage.saveSecureData("last_weather_$cityName", weatherData)

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    resultTextView.text = weatherData
                    statusTextView.text = "Data fetched successfully for $cityName"
                    statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
                }

            } catch (e: NetworkException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Network unavailable. Please check your connection.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Failed to fetch data. Please try again.")
                }
                Log.e(TAG, "Error fetching data", e)
            }
        }
    }

    /**
     * Simulates API call to fetch weather data (in real app, use Retrofit)
     * @param cityName City name
     * @return Formatted weather data string
     * @throws NetworkException if simulation fails
     */
    private suspend fun fetchWeatherData(cityName: String): String {
        // Simulate network delay
        kotlinx.coroutines.delay(2000)

        // Simulate network failure 20% of time
        if (Random().nextFloat() < 0.2f) {
            throw NetworkException("Simulated network failure")
        }

        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        return "🌤️ Weather for $cityName\nTemperature: 22°C\nHumidity: 65%\nUpdated: $timestamp"
    }

    /**
     * Requests camera permission following Android best practices
     */
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    showError("Camera permission denied. Cannot capture images.")
                }
            }
        }
    }

    /**
     * Opens camera for image capture
     */
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST)
        } else {
            showError("No camera app available on this device.")
        }
    }

    /**
     * Shows/hides loading indicator
     * @param isLoading true to show loading, false to hide
     */
    private fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) ProgressBar.VISIBLE else ProgressBar.GONE
        fetchDataButton.isEnabled = !isLoading
    }

    /**
     * Displays user-friendly error message (security best practice - no technical details)
     * @param message Error message to display
     */
    private fun showError(message: String) {
        statusTextView.text = message
        statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Custom exception for network failures
 */
class NetworkException(message: String) : Exception(message)