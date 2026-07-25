# ProGuard configuration for AI Voice Keyboard

# Keep Google Generative AI classes
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Keep Kotlin data classes
-keepclassmembers class ** {
    *** **(***);
}

# Keep Android framework classes
-keep class android.inputmethodservice.InputMethodService { *; }
-keep class android.speech.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep callback interfaces
-keep interface com.google.ai.client.generativeai.** { *; }
-keep interface android.speech.RecognitionListener { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep exception messages
-renamesourcefileattribute SourceFile
