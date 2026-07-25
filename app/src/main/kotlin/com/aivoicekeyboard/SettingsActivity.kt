package com.aivoicekeyboard

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyEditText: EditText
    private lateinit var systemPromptEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        apiKeyEditText = findViewById(R.id.api_key_input)
        systemPromptEditText = findViewById(R.id.system_prompt_input)
        saveButton = findViewById(R.id.save_button)
        resetButton = findViewById(R.id.reset_button)

        loadSettings()

        saveButton.setOnClickListener { saveSettings() }
        resetButton.setOnClickListener { resetToDefaults() }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("ai_keyboard_prefs", Context.MODE_PRIVATE)
        
        val savedApiKey = sharedPreferences.getString("gemini_api_key", "") ?: ""
        val savedSystemPrompt = sharedPreferences.getString("system_prompt", getDefaultSystemPrompt()) ?: getDefaultSystemPrompt()

        apiKeyEditText.setText(savedApiKey)
        systemPromptEditText.setText(savedSystemPrompt)
    }

    private fun saveSettings() {
        val apiKey = apiKeyEditText.text.toString().trim()
        val systemPrompt = systemPromptEditText.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter a Gemini API Key", Toast.LENGTH_SHORT).show()
            return
        }

        if (systemPrompt.isEmpty()) {
            Toast.makeText(this, "System prompt cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("ai_keyboard_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("gemini_api_key", apiKey)
            putString("system_prompt", systemPrompt)
            apply()
        }

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        systemPromptEditText.setText(getDefaultSystemPrompt())
        Toast.makeText(this, "System prompt reset to default", Toast.LENGTH_SHORT).show()
    }

    private fun getDefaultSystemPrompt(): String {
        return """You are a natural, conversational AI assistant integrated into an Android keyboard. 
Your responses should be:
- Direct and concise, no AI clichés or formal preambles
- Context-aware based on the user's current application
- Conversational and human-like, as if texting with a knowledgeable friend
- Free from phrases like "I appreciate your question" or "As an AI language model"
- Practical and immediately useful for the user's current task

Respond naturally without excessive politeness or artificial warmth."""
    }
}
