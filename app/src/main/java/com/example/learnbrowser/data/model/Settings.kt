package com.example.learnbrowser.data.model

import com.example.learnbrowser.data.translation.TranslationServiceType

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
    val downloadedLanguages: List<String> = emptyList(),
    
    /**
     * The translation service to use
     */
    val translationService: TranslationServiceType = TranslationServiceType.getDefault(),
    
    /**
     * API keys for each translation service
     */
    val translationApiKeys: Map<TranslationServiceType, String> = mapOf(
        TranslationServiceType.GOOGLE_TRANSLATE to "",
        TranslationServiceType.LIBRE_TRANSLATE to "",
        TranslationServiceType.DEEPL to "",
        TranslationServiceType.MICROSOFT_TRANSLATOR to "",
        TranslationServiceType.YANDEX_TRANSLATE to ""
    ),
    
    /**
     * Custom endpoints for services that support them (like LibreTranslate)
     */
    val customEndpoints: Map<TranslationServiceType, String> = mapOf(
        TranslationServiceType.LIBRE_TRANSLATE to "https://libretranslate.com/translate"
    )
)
