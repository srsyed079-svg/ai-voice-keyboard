# AI Voice Keyboard - Android Custom IME with Gemini 2.5 API

An advanced Android Input Method Editor (IME) that integrates Google's Gemini 2.5 API for intelligent voice-to-text, screen analysis, and context-aware responses.

## Features

### 🎤 Voice Input
- Real-time speech-to-text conversion using Android's SpeechRecognizer
- Automatic voice detection and processing
- Multi-language support

### 🤖 Gemini 2.5 AI Integration
- Direct integration with Google's latest Gemini 2.5 Pro model
- Custom system prompts for personalized AI behavior
- Context-aware responses without AI clichés
- Natural, human-like text generation

### 📷 Screen Capture & Vision
- Real-time screen capture via MediaProjection API
- Vision analysis of captured screens
- Integration with Gemini's multimodal capabilities

### 🔄 Dual Mode Operation

#### Mode A: Inject
- AI-generated responses automatically injected into active text fields
- Seamless integration with any text input field
- Transparent operation

#### Mode B: Private
- Responses displayed in a private overlay window
- Automatic text-to-speech (TTS) playback
- Auto-dismiss with manual close option
- Isolated from active application

### 🎙️ Text-to-Speech
- Automatic audio feedback for generated responses
- Locale-aware voice synthesis
- Adjustable speech rate and pitch

### ⚙️ Customization
- Store and manage Gemini API Key securely in SharedPreferences
- Custom system prompts to define AI behavior
- Mode switching without configuration
- Per-application settings

## Architecture

### Core Components

**AIVoiceKeyboardService.kt**
- Main IME service extending `InputMethodService`
- Handles voice recognition via `SpeechRecognizer`
- Manages Gemini API calls asynchronously
- Controls text injection and overlay rendering
- Implements dual-mode response handling

**SettingsActivity.kt**
- Settings UI for API key configuration
- Custom system prompt editor
- Persistent storage via SharedPreferences
- Reset to defaults functionality

**MainActivity.kt**
- Launcher activity with setup instructions
- Quick access to Settings and IME configuration
- Feature overview and documentation

### Data Flow

```
User Voice Input
    ↓
[SpeechRecognizer] → Recognized Text
    ↓
[Gemini 2.5 API] + [Custom System Prompt] → AI Response
    ↓
Mode Selection
    ├→ [Inject Mode] → commitText() → Active Text Field
    └→ [Private Mode] → Overlay + TTS → User Hears Response
```

## Setup & Installation

### Prerequisites
- Android API 24+ (Android 7.0 Marshmallow)
- Gemini API Key (get from [Google AI Studio](https://aistudio.google.com/app/apikey))
- Android Studio with Kotlin support

### Step 1: Add Gemini API Key
1. Launch the app
2. Click "Settings"
3. Paste your Gemini API Key
4. Customize the system prompt (optional)
5. Click "Save"

### Step 2: Enable IME
1. Go to **Settings > Languages & Input > Keyboard (IME)**
2. Enable "AI Voice Keyboard"
3. Select it as your active keyboard

### Step 3: Use
- Click the 🎤 button to start voice input
- Click the 📷 button to analyze screen
- Toggle between Inject ↔ Private mode
- Click ⚙️ to adjust settings

## Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Dependencies

- **Gemini AI SDK**: `com.google.ai.client.generativeai:google-generative-ai-kotlin:0.1.2`
- **AndroidX**: appcompat, core-ktx, lifecycle
- **Kotlin Coroutines**: For async operations
- **Material Design**: UI components

## Default System Prompt

```
You are a natural, conversational AI assistant integrated into an Android keyboard. 
Your responses should be:
- Direct and concise, no AI clichés or formal preambles
- Context-aware based on the user's current application
- Conversational and human-like, as if texting with a knowledgeable friend
- Free from phrases like "I appreciate your question" or "As an AI language model"
- Practical and immediately useful for the user's current task

Respond naturally without excessive politeness or artificial warmth.
```

## Security Considerations

⚠️ **API Key Storage**
- API keys are stored in SharedPreferences with `Context.MODE_PRIVATE`
- For production, consider encrypted storage using Android Keystore
- Never hardcode API keys in source code

⚠️ **Permissions**
- `SYSTEM_ALERT_WINDOW` is required for overlay rendering in Private mode
- `RECORD_AUDIO` requires user runtime permission (Android 6.0+)
- `CAMERA` requires user runtime permission for screen analysis

## Development

### Project Structure
```
ai-voice-keyboard/
├── app/src/main/
│   ├── kotlin/com/aivoicekeyboard/
│   │   ├── AIVoiceKeyboardService.kt
│   │   ├── SettingsActivity.kt
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── layout/
│   │   ├── values/
│   │   ├── drawable/
│   │   └── xml/
│   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── proguard-rules.pro
```

### Build & Run

**Debug Build**
```bash
./gradlew installDebug
```

**Release Build**
```bash
./gradlew assembleRelease
```

**Run Tests**
```bash
./gradlew test
```

## Troubleshooting

### "API key not configured"
- Ensure you've added your Gemini API key in Settings
- Keys must be at least 20 characters
- Verify the key is from Google AI Studio

### "Speech recognition error"
- Ensure `RECORD_AUDIO` permission is granted
- Check if device has Google Play Services
- Try using Google's speech recognition app directly

### Overlay not appearing in Private mode
- Grant `SYSTEM_ALERT_WINDOW` permission
- Check if overlay is being blocked by another app
- Ensure device API is 24+

### Slow response time
- Network connectivity may be affected
- Gemini API might be throttled
- Check API quota on Google AI Studio
- System prompt length affects processing time

## Performance Optimization

- Voice recognition runs on UI thread (handled by Android framework)
- Gemini API calls use Coroutines on `Dispatchers.IO`
- Response overlay uses `WindowManager` for efficient rendering
- TTS operations are asynchronous to prevent UI blocking

## Future Enhancements

- [ ] Screen capture integration with vision analysis
- [ ] Multi-language response support
- [ ] Response history and caching
- [ ] Custom keyboard layout options
- [ ] Gesture-based shortcuts
- [ ] Response editing before injection
- [ ] Batch voice commands
- [ ] Integration with device context (calendar, contacts, etc.)

## License

This project is provided as-is for educational and personal use.

## Support & Contribution

For issues, feature requests, or contributions, please contact the development team.

---

**Created with ❤️ for Android developers**  
*Powered by Google Gemini 2.5 API*
