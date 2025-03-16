package com.example.learnbrowser.data.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Implementation of the TranslationServiceInterface using DeepL API.
 */
class DeepLTranslateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKey: String
) : TranslationServiceInterface {

    private val baseUrl = "https://api-free.deepl.com/v2"

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/translate")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Authorization", "DeepL-Auth-Key $apiKey")
            connection.doOutput = true
            
            // Create request body
            val requestBody = StringBuilder()
                .append("text=").append(text.encodeUrl())
                .append("&target_lang=").append(targetLanguage.uppercase())
            
            if (sourceLanguage != null) {
                requestBody.append("&source_lang=").append(sourceLanguage.uppercase())
            }
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val translations = jsonResponse.getJSONArray("translations")
                if (translations.length() > 0) {
                    translations.getJSONObject(0).getString("text")
                } else {
                    text
                }
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
            // DeepL doesn't have a separate language detection endpoint
            // We'll use the translate endpoint with a dummy target language
            val url = URL("$baseUrl/translate")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Authorization", "DeepL-Auth-Key $apiKey")
            connection.doOutput = true
            
            // Create request body
            val requestBody = StringBuilder()
                .append("text=").append(text.encodeUrl())
                .append("&target_lang=EN")
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val translations = jsonResponse.getJSONArray("translations")
                if (translations.length() > 0) {
                    val detectedLanguage = translations.getJSONObject(0).getString("detected_source_language")
                    detectedLanguage.lowercase()
                } else {
                    "en"
                }
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
            val url = URL("$baseUrl/languages?type=source")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "DeepL-Auth-Key $apiKey")
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                val languages = mutableListOf<String>()
                
                for (i in 0 until jsonArray.length()) {
                    val language = jsonArray.getJSONObject(i)
                    languages.add(language.getString("language").lowercase())
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
            // DeepL API doesn't support downloading language models
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
