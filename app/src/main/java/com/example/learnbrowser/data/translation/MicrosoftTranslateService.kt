package com.example.learnbrowser.data.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of the TranslationServiceInterface using Microsoft Translator API.
 */
class MicrosoftTranslateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKey: String
) : TranslationServiceInterface {

    private val baseUrl = "https://api.cognitive.microsofttranslator.com"
    private val region = "global" // Change this to your Azure region if needed

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/translate?api-version=3.0&to=$targetLanguage${if (sourceLanguage != null) "&from=$sourceLanguage" else ""}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", apiKey)
            connection.setRequestProperty("Ocp-Apim-Subscription-Region", region)
            connection.setRequestProperty("X-ClientTraceId", UUID.randomUUID().toString())
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONArray().apply {
                put(JSONObject().apply {
                    put("Text", text)
                })
            }
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                if (jsonArray.length() > 0) {
                    val translations = jsonArray.getJSONObject(0).getJSONArray("translations")
                    if (translations.length() > 0) {
                        translations.getJSONObject(0).getString("text")
                    } else {
                        text
                    }
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
            val url = URL("$baseUrl/detect?api-version=3.0")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", apiKey)
            connection.setRequestProperty("Ocp-Apim-Subscription-Region", region)
            connection.setRequestProperty("X-ClientTraceId", UUID.randomUUID().toString())
            connection.doOutput = true
            
            // Create request body
            val requestBody = JSONArray().apply {
                put(JSONObject().apply {
                    put("Text", text)
                })
            }
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                if (jsonArray.length() > 0) {
                    jsonArray.getJSONObject(0).getString("language")
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
            val url = URL("$baseUrl/languages?api-version=3.0&scope=translation")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val translation = jsonResponse.getJSONObject("translation")
                val languages = mutableListOf<String>()
                
                val keys = translation.keys()
                while (keys.hasNext()) {
                    languages.add(keys.next())
                }
                
                languages
            } else {
                // Return a default list of common languages
                listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh-Hans", "ko")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a default list of common languages
            listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh-Hans", "ko")
        }
    }

    override suspend fun downloadLanguageModel(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Microsoft Translator API doesn't support downloading language models
            // This is a placeholder implementation
            kotlinx.coroutines.delay(2000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
