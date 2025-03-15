package com.example.learnbrowser.data.model

/**
 * Data class representing user settings for the application.
 */
data class Settings(
    /**
     * The target language code for translations (e.g., "es" for Spanish)
     */
    val targetLanguage: String = "en",
    
    /**
     * Whether to automatically translate pages when loaded
     */
    val autoTranslatePages: Boolean = false,
    
    /**
     * List of downloaded language codes for offline use
     */
    val downloadedLanguages: List<String> = emptyList()
)
