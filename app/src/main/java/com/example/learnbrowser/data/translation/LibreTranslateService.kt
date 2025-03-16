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
 * Implementation of the TranslationServiceInterface using LibreTranslate API.
 */
class LibreTranslateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKey: String,
    private val endpoint: String
) : TranslationServiceInterface {

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("q", text)
                put("source", sourceLanguage ?: "auto")
                put("target", targetLanguage)
                put("format", "text")
                if (apiKey.isNotEmpty()) {
                    put("api_key", apiKey)
                }
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
                jsonResponse.getString("translatedText")
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
            val url = URL("$endpoint/detect")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("q", text)
                if (apiKey.isNotEmpty()) {
                    put("api_key", apiKey)
                }
            }
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONObject(response).getJSONArray("detections")
                if (jsonArray.length() > 0) {
                    val detection = jsonArray.getJSONObject(0)
                    detection.getString("language")
                } else {
                    "en" // Default to English if no detection
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
            val url = URL("$endpoint/languages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONObject(response).getJSONArray("languages")
                val languages = mutableListOf<String>()
                
                for (i in 0 until jsonArray.length()) {
                    val language = jsonArray.getJSONObject(i)
                    languages.add(language.getString("code"))
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
            // LibreTranslate API doesn't support downloading language models
            // This is a placeholder implementation
            kotlinx.coroutines.delay(2000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
