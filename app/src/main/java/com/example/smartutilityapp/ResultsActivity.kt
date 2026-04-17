/**
 * ResultsActivity - Second screen showing stored results
 * Demonstrates navigation between screens
 */
package com.example.smartutilityapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val secureStorage = SecureStorageHelper(this)
        val resultsTextView: TextView = findViewById(R.id.resultsTextView)
        val backButton: Button = findViewById(R.id.backButton)

        // Display last saved weather data
        val lastWeather = secureStorage.getSecureData("last_weather_Toronto")
            ?: "No data available. Fetch data from main screen first."
        resultsTextView.text = lastWeather

        backButton.setOnClickListener {
            finish()
        }
    }
}