package com.example.learnbrowser.data.translation

/**
 * Interface for translation services.
 */
interface TranslationServiceInterface {
    /**
     * Translate a single word or phrase.
     *
     * @param text The text to translate
     * @param sourceLanguage The source language code (can be null for auto-detection)
     * @param targetLanguage The target language code
     * @return The translated text
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String

    /**
     * Detect the language of a text.
     *
     * @param text The text to detect the language of
     * @return The detected language code
     */
    suspend fun detectLanguage(text: String): String

    /**
     * Get a list of supported languages.
     *
     * @return A list of language codes
     */
    suspend fun getSupportedLanguages(): List<String>

    /**
     * Download a language model for offline use.
     *
     * @param languageCode The language code to download
     * @return True if the download was successful, false otherwise
     */
    suspend fun downloadLanguageModel(languageCode: String): Boolean
}
