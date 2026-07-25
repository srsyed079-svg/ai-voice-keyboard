package com.aivoicekeyboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AIVoiceKeyboardService : InputMethodService(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var windowManager: WindowManager
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var generativeModel: GenerativeModel? = null

    private var currentMode = KeyboardMode.INJECT
    private var isListening = false
    private var mediaProjectionAvailable = false

    private var responseView: FrameLayout? = null
    private var responseTextView: TextView? = null
    private var responseScrollView: ScrollView? = null

    private val handler = Handler(Looper.getMainLooper())

    enum class KeyboardMode {
        INJECT, PRIVATE
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("ai_keyboard_prefs", Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(SpeechRecognitionListener())

        textToSpeech = TextToSpeech(this, this)
        initializeGeminiModel()
    }

    override fun onCreateInputView(): View {
        val inflater = LayoutInflater.from(this)
        val keyboardLayout = inflater.inflate(R.layout.keyboard_layout, null) as ViewGroup

        val voiceButton = keyboardLayout.findViewById<ImageButton>(R.id.voice_button)
        val cameraButton = keyboardLayout.findViewById<ImageButton>(R.id.camera_button)
        val modeToggle = keyboardLayout.findViewById<ToggleButton>(R.id.mode_toggle)
        val settingsButton = keyboardLayout.findViewById<Button>(R.id.settings_button)

        voiceButton.setOnClickListener { startVoiceInput() }
        cameraButton.setOnClickListener { captureScreenAndAnalyze() }
        modeToggle.setOnCheckedChangeListener { _, isChecked ->
            currentMode = if (isChecked) KeyboardMode.PRIVATE else KeyboardMode.INJECT
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

        return keyboardLayout
    }

    private fun startVoiceInput() {
        if (isListening) return

        isListening = true
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message...")
        }

        speechRecognizer.startListening(recognizerIntent)
    }

    private fun captureScreenAndAnalyze() {
        Toast.makeText(this, "Screen capture feature requires MediaProjection setup", Toast.LENGTH_SHORT).show()
    }

    private fun initializeGeminiModel() {
        val apiKey = sharedPreferences.getString("gemini_api_key", "") ?: ""
        if (apiKey.isNotEmpty()) {
            try {
                generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-pro",
                    apiKey = apiKey
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to initialize Gemini API", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun generateResponse(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (generativeModel == null) {
                    return@withContext "API key not configured. Please set it in settings."
                }

                val systemPrompt = sharedPreferences.getString("system_prompt", getDefaultSystemPrompt()) ?: getDefaultSystemPrompt()
                val fullPrompt = "$systemPrompt\n\nUser: $userMessage"

                val response = generativeModel!!.generateContent(
                    content {
                        text(fullPrompt)
                    }
                )

                response.text ?: "No response generated"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
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

    private fun processRecognizedSpeech(recognizedText: String) {
        lifecycleScope.launch {
            val response = generateResponse(recognizedText)
            handler.post {
                when (currentMode) {
                    KeyboardMode.INJECT -> injectTextToActiveField(response)
                    KeyboardMode.PRIVATE -> showPrivateResponse(response)
                }
            }
        }
    }

    private fun injectTextToActiveField(text: String) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    private fun showPrivateResponse(text: String) {
        if (responseView == null) {
            createResponseOverlay()
        }

        responseTextView?.text = text
        responseView?.visibility = View.VISIBLE
        responseScrollView?.scrollTo(0, 0)

        // Auto-read the response
        if (::textToSpeech.isInitialized && textToSpeech.isSpeaking.not()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }

        // Auto-dismiss after 10 seconds
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            responseView?.visibility = View.GONE
        }, 10000)
    }

    private fun createResponseOverlay() {
        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = 800
            height = 400
            gravity = Gravity.CENTER
        }

        responseView = FrameLayout(this).apply {
            setBackgroundColor(0xCC000000.toInt())
        }

        responseScrollView = ScrollView(this).apply {
            responseTextView = TextView(this@AIVoiceKeyboardService).apply {
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 16f
                setPadding(20, 20, 20, 20)
            }
            addView(responseTextView)
        }

        responseView?.addView(responseScrollView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        windowManager.addView(responseView, params)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        responseView?.let { windowManager.removeView(it) }
    }

    inner class SpeechRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            isListening = false
            Toast.makeText(this@AIVoiceKeyboardService, "Speech recognition error: $error", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: android.os.Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                processRecognizedSpeech(recognizedText)
            }
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onEvaluateInputViewShown(): Boolean = true
}
