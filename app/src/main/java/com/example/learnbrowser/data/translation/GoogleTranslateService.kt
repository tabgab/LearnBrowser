package com.example.learnbrowser.data.translation

import android.content.Context
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of the TranslationServiceInterface using Google Cloud Translation API.
 */
class GoogleTranslateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKey: String
) : TranslationServiceInterface {

    // Lazy initialization of the Translate client
    private val translateClient: Translate by lazy {
        TranslateOptions.newBuilder()
            .setApiKey(apiKey)
            .build()
            .service
    }

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val translation: Translation = if (sourceLanguage != null) {
                translateClient.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(sourceLanguage),
                    Translate.TranslateOption.targetLanguage(targetLanguage),
                    Translate.TranslateOption.model("base")
                )
            } else {
                translateClient.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage),
                    Translate.TranslateOption.model("base")
                )
            }
            
            translation.translatedText
        } catch (e: Exception) {
            // In case of an error, return the original text with an error indicator
            e.printStackTrace()
            "$text [ERROR: ${e.message}]"
        }
    }

    override suspend fun detectLanguage(text: String): String = withContext(Dispatchers.IO) {
        try {
            val detection = translateClient.detect(text)
            detection.language
        } catch (e: Exception) {
            e.printStackTrace()
            // Default to English if detection fails
            "en"
        }
    }

    override suspend fun getSupportedLanguages(): List<String> = withContext(Dispatchers.IO) {
        try {
            translateClient.listSupportedLanguages()
                .map { it.code }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a default list of common languages
            listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh", "ko")
        }
    }

    override suspend fun downloadLanguageModel(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Google Translate API doesn't support downloading language models
            // This is a placeholder implementation
            kotlinx.coroutines.delay(2000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
