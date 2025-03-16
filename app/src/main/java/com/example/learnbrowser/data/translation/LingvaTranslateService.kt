package com.example.learnbrowser.data.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Implementation of the TranslationServiceInterface using Lingva Translate API.
 * Lingva is a free alternative front-end for Google Translate that doesn't require an API key.
 */
class LingvaTranslateService @Inject constructor(
    @ApplicationContext private val context: Context
) : TranslationServiceInterface {

    private val baseUrl = "https://lingva.ml/api/v1"

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val source = sourceLanguage ?: "auto"
            val url = URL("$baseUrl/${source}/${targetLanguage}/${text.encodeUrl()}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getString("translation")
            } else {
                "$text [ERROR: HTTP $responseCode]"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "$text [ERROR: ${e.message}]"
        }
    }

    override suspend fun detectLanguage(text: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/detect/${text.encodeUrl()}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getString("language")
            } else {
                "en" // Default to English on error
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "en" // Default to English on error
        }
    }

    override suspend fun getSupportedLanguages(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/languages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val languages = mutableListOf<String>()
                
                val languagesObj = jsonResponse.getJSONObject("languages")
                val keys = languagesObj.keys()
                
                while (keys.hasNext()) {
                    languages.add(keys.next())
                }
                
                languages
            } else {
                // Return a default list of common languages
                listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh", "ko")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a default list of common languages
            listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh", "ko")
        }
    }

    override suspend fun downloadLanguageModel(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Lingva Translate API doesn't support downloading language models
            // This is a placeholder implementation
            kotlinx.coroutines.delay(2000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // Helper function to URL encode text
    private fun String.encodeUrl(): String {
        return java.net.URLEncoder.encode(this, "UTF-8")
    }
}
