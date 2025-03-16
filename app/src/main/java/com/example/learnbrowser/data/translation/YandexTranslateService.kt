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
 * Implementation of the TranslationServiceInterface using Yandex Translate API.
 */
class YandexTranslateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKey: String
) : TranslationServiceInterface {

    private val baseUrl = "https://translate.api.cloud.yandex.net/translate/v2"

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/translate")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Api-Key $apiKey")
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("texts", JSONArray().apply { put(text) })
                put("targetLanguageCode", targetLanguage)
                if (sourceLanguage != null) {
                    put("sourceLanguageCode", sourceLanguage)
                }
                put("format", "PLAIN_TEXT")
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
            val url = URL("$baseUrl/detect")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Api-Key $apiKey")
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("text", text)
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
                jsonResponse.getString("languageCode")
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
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Api-Key $apiKey")
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("folder_id", "")
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
                val languages = jsonResponse.getJSONArray("languages")
                val languageCodes = mutableListOf<String>()
                
                for (i in 0 until languages.length()) {
                    val language = languages.getJSONObject(i)
                    languageCodes.add(language.getString("code"))
                }
                
                languageCodes
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
            // Yandex Translate API doesn't support downloading language models
            // This is a placeholder implementation
            kotlinx.coroutines.delay(2000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
