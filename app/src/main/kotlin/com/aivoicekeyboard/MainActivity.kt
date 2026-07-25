package com.aivoicekeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val descriptionText = findViewById<TextView>(R.id.description_text)
        val settingsButton = findViewById<Button>(R.id.open_settings_button)
        val imeSettingsButton = findViewById<Button>(R.id.open_ime_settings_button)

        descriptionText.text = """
            AI Voice Keyboard - Powered by Gemini 2.5 API
            
            Features:
            • Voice-to-text input with AI enhancement
            • Screen capture & vision analysis
            • Dual mode: Inject directly or show privately
            • Custom system prompts
            • Text-to-speech responses
            
            Setup:
            1. Add your Gemini API key in Settings
            2. Go to Settings > Languages & Input > Keyboard
            3. Enable "AI Voice Keyboard"
            4. Select it as your active keyboard
        """.trimIndent()

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        imeSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
    }
}
