package com.example.learnbrowser.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnbrowser.R
import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.repository.SettingsRepository
import com.example.learnbrowser.data.translation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the SettingsActivity.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val translationService: TranslationService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    init {
        // Initialize API keys from BuildConfig
        viewModelScope.launch {
            try {
                // Call the initializeApiKeysFromBuildConfig method directly
                settingsRepository.preferencesManager.initializeApiKeysFromBuildConfig()
            } catch (e: Exception) {
                // Log the error but don't crash the app
                e.printStackTrace()
            }
        }
    }

    // User settings
    val settings = settingsRepository.userSettings.asLiveData()
    
    // Language download state
    private val _languageDownloadState = MutableLiveData<Pair<String, Boolean>>()
    val languageDownloadState: LiveData<Pair<String, Boolean>> = _languageDownloadState
    
    // Error handling
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Update the target language.
     *
     * @param languageCode The language code to set as the target language
     */
    fun updateTargetLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTargetLanguage(languageCode)
            } catch (e: Exception) {
                _error.postValue("Failed to update target language: ${e.message}")
            }
        }
    }

    /**
     * Update the auto-translate pages setting.
     *
     * @param autoTranslate Whether to auto-translate pages
     */
    fun updateAutoTranslatePages(autoTranslate: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateAutoTranslatePages(autoTranslate)
            } catch (e: Exception) {
                _error.postValue("Failed to update auto-translate setting: ${e.message}")
            }
        }
    }

    /**
     * Update the translation service.
     *
     * @param serviceType The translation service to use
     */
    fun updateTranslationService(serviceType: TranslationServiceType) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTranslationService(serviceType)
            } catch (e: Exception) {
                _error.postValue("Failed to update translation service: ${e.message}")
            }
        }
    }

    /**
     * Update an API key for a translation service.
     *
     * @param serviceType The translation service
     * @param apiKey The API key
     */
    fun updateTranslationApiKey(serviceType: TranslationServiceType, apiKey: String) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTranslationApiKey(serviceType, apiKey)
            } catch (e: Exception) {
                _error.postValue("Failed to update API key: ${e.message}")
            }
        }
    }

    /**
     * Update a custom endpoint for a translation service.
     *
     * @param serviceType The translation service
     * @param endpoint The custom endpoint
     */
    fun updateCustomEndpoint(serviceType: TranslationServiceType, endpoint: String) {
        viewModelScope.launch {
            try {
                settingsRepository.updateCustomEndpoint(serviceType, endpoint)
            } catch (e: Exception) {
                _error.postValue("Failed to update custom endpoint: ${e.message}")
            }
        }
    }

    /**
     * Download a language model for offline use.
     *
     * @param languageCode The language code to download
     */
    fun downloadLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                val success = translationService.downloadLanguageModel(languageCode)
                
                if (success) {
                    // Add the language to the downloaded languages list
                    settingsRepository.addDownloadedLanguage(languageCode)
                    _languageDownloadState.postValue(Pair(languageCode, true))
                } else {
                    _languageDownloadState.postValue(Pair(languageCode, false))
                    _error.postValue("Failed to download language model")
                }
            } catch (e: Exception) {
                _languageDownloadState.postValue(Pair(languageCode, false))
                _error.postValue("Failed to download language model: ${e.message}")
            }
        }
    }

    /**
     * Check if a language is already downloaded.
     *
     * @param languageCode The language code to check
     * @return True if the language is downloaded, false otherwise
     */
    suspend fun isLanguageDownloaded(languageCode: String): Boolean {
        val currentSettings = settingsRepository.userSettings.first()
        return currentSettings.downloadedLanguages.contains(languageCode)
    }

    /**
     * Get a list of supported languages for the current translation service.
     *
     * @return A list of language codes
     */
    suspend fun getSupportedLanguages(): List<String> {
        return try {
            translationService.getSupportedLanguages()
        } catch (e: Exception) {
            _error.postValue("Failed to get supported languages: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get a list of supported languages for a specific translation service.
     *
     * @param serviceType The translation service type
     * @return A list of language codes
     */
    suspend fun getSupportedLanguagesForService(serviceType: TranslationServiceType): List<String> {
        return try {
            // Create a temporary instance of the service to get its supported languages
            when (serviceType) {
                TranslationServiceType.GOOGLE_TRANSLATE -> {
                    val apiKey = getTranslationApiKey(serviceType)
                    GoogleTranslateService(context, apiKey).getSupportedLanguages()
                }
                TranslationServiceType.LIBRE_TRANSLATE -> {
                    val apiKey = getTranslationApiKey(serviceType)
                    val endpoint = getCustomEndpoint(serviceType)
                    val finalEndpoint = if (endpoint.isBlank()) "https://libretranslate.com/translate" else endpoint
                    LibreTranslateService(context, apiKey, finalEndpoint).getSupportedLanguages()
                }
                TranslationServiceType.DEEPL -> {
                    val apiKey = getTranslationApiKey(serviceType)
                    DeepLTranslateService(context, apiKey).getSupportedLanguages()
                }
                TranslationServiceType.LINGVA_TRANSLATE -> {
                    LingvaTranslateService(context).getSupportedLanguages()
                }
                TranslationServiceType.ARGOS_TRANSLATE -> {
                    ArgosTranslateService(context).getSupportedLanguages()
                }
                TranslationServiceType.MICROSOFT_TRANSLATOR -> {
                    val apiKey = getTranslationApiKey(serviceType)
                    MicrosoftTranslateService(context, apiKey).getSupportedLanguages()
                }
                TranslationServiceType.YANDEX_TRANSLATE -> {
                    val apiKey = getTranslationApiKey(serviceType)
                    YandexTranslateService(context, apiKey).getSupportedLanguages()
                }
            }
        } catch (e: Exception) {
            _error.postValue("Failed to get supported languages: ${e.message}")
            // Return a default list of common languages
            listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "zh", "ko")
        }
    }

    /**
     * Get a list of available translation services.
     *
     * @return A list of translation service types
     */
    fun getAvailableTranslationServices(): List<TranslationServiceType> {
        return TranslationServiceType.values().toList()
    }

    /**
     * Get the current API key for a translation service.
     *
     * @param serviceType The translation service
     * @return The API key
     */
    suspend fun getTranslationApiKey(serviceType: TranslationServiceType): String {
        val currentSettings = settingsRepository.userSettings.first()
        return currentSettings.translationApiKeys[serviceType] ?: ""
    }

    /**
     * Get the current custom endpoint for a translation service.
     *
     * @param serviceType The translation service
     * @return The custom endpoint
     */
    suspend fun getCustomEndpoint(serviceType: TranslationServiceType): String {
        val currentSettings = settingsRepository.userSettings.first()
        return currentSettings.customEndpoints[serviceType] ?: ""
    }
    
    /**
     * Update the UI language.
     *
     * @param languageCode The language code to set as the UI language
     */
    fun updateUiLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                // Debug logging
                android.util.Log.d("SettingsViewModel", "Updating UI language to: $languageCode")
                
                settingsRepository.updateUiLanguage(languageCode)
                
                // Debug logging
                val currentLanguage = settingsRepository.getUiLanguage()
                android.util.Log.d("SettingsViewModel", "Current UI language after update: $currentLanguage")
            } catch (e: Exception) {
                _error.postValue("Failed to update UI language: ${e.message}")
                
                // Debug logging
                android.util.Log.e("SettingsViewModel", "Failed to update UI language", e)
            }
        }
    }
    
    /**
     * Get the current UI language.
     *
     * @return The current UI language code
     */
    suspend fun getUiLanguage(): String {
        return settingsRepository.getUiLanguage()
    }
    
    /**
     * Get a list of available UI languages.
     * Only returns languages that have actual translations implemented.
     *
     * @return A list of language codes and their display names
     */
    fun getAvailableUiLanguages(context: Context): List<Pair<String, String>> {
        val languageCodes = context.resources.getStringArray(R.array.ui_language_codes)
        val languageNames = context.resources.getStringArray(R.array.ui_languages)
        
        // Get all the implemented languages (those with resource files)
        val implementedLanguages = getImplementedLanguages(context)
        
        // Filter the language codes and names to only include implemented languages
        return languageCodes.zip(languageNames)
            .filter { (code, _) -> implementedLanguages.contains(code) }
    }
    
    /**
     * Get a list of languages that have actual translations implemented.
     * This checks for the existence of values-XX resource directories.
     *
     * @param context The context to use for accessing resources
     * @return A list of language codes that have translations
     */
    private fun getImplementedLanguages(context: Context): List<String> {
        // English is always available as the default language
        val implementedLanguages = mutableListOf("en")
        
        // Check for the existence of each language's resource file
        val languagesToCheck = listOf(
            "de", "fr", "hu", "it", "es", "ja", "zh", "ko", 
            "hi", "ur", "ar", "pt", "pl", "uk", "ru"
        )
        
        for (lang in languagesToCheck) {
            try {
                // Try to get the app_name string from the language's resource file
                val resourceId = context.resources.getIdentifier(
                    "app_name", 
                    "string", 
                    context.packageName
                )
                
                // Set the configuration to the language we're checking
                val config = Configuration(context.resources.configuration)
                config.setLocale(Locale(lang))
                val localizedContext = context.createConfigurationContext(config)
                
                // If we can get the string without an exception, the language is implemented
                localizedContext.resources.getString(resourceId)
                implementedLanguages.add(lang)
            } catch (e: Exception) {
                // If an exception occurs, the language is not implemented
                android.util.Log.d("SettingsViewModel", "Language $lang is not implemented")
            }
        }
        
        return implementedLanguages
    }
}
