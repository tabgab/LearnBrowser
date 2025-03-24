package com.example.learnbrowser.data.repository

import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.preferences.PreferencesManager
import com.example.learnbrowser.data.translation.TranslationServiceType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for settings-related operations.
 * This class serves as a single source of truth for user settings.
 */
@Singleton
class SettingsRepository @Inject constructor(
    val preferencesManager: PreferencesManager
) {
    /**
     * Get the user settings as a Flow.
     */
    val userSettings: Flow<Settings> = preferencesManager.userSettingsFlow

    /**
     * Update the target language.
     *
     * @param languageCode The language code to set as the target language
     */
    suspend fun updateTargetLanguage(languageCode: String) {
        preferencesManager.updateTargetLanguage(languageCode)
    }

    /**
     * Update the auto-translate pages setting.
     *
     * @param autoTranslate Whether to auto-translate pages
     */
    suspend fun updateAutoTranslatePages(autoTranslate: Boolean) {
        preferencesManager.updateAutoTranslatePages(autoTranslate)
    }

    /**
     * Update the translation service.
     *
     * @param serviceType The translation service to use
     */
    suspend fun updateTranslationService(serviceType: TranslationServiceType) {
        preferencesManager.updateTranslationService(serviceType)
    }

    /**
     * Update an API key for a translation service.
     *
     * @param serviceType The translation service
     * @param apiKey The API key
     */
    suspend fun updateTranslationApiKey(serviceType: TranslationServiceType, apiKey: String) {
        preferencesManager.updateTranslationApiKey(serviceType, apiKey)
    }

    /**
     * Update a custom endpoint for a translation service.
     *
     * @param serviceType The translation service
     * @param endpoint The custom endpoint
     */
    suspend fun updateCustomEndpoint(serviceType: TranslationServiceType, endpoint: String) {
        preferencesManager.updateCustomEndpoint(serviceType, endpoint)
    }

    /**
     * Add a language to the downloaded languages list.
     *
     * @param languageCode The language code to add
     */
    suspend fun addDownloadedLanguage(languageCode: String) {
        preferencesManager.addDownloadedLanguage(languageCode)
    }

    /**
     * Remove a language from the downloaded languages list.
     *
     * @param languageCode The language code to remove
     */
    suspend fun removeDownloadedLanguage(languageCode: String) {
        preferencesManager.removeDownloadedLanguage(languageCode)
    }

    /**
     * Clear all downloaded languages.
     */
    suspend fun clearDownloadedLanguages() {
        preferencesManager.clearDownloadedLanguages()
    }
    
    /**
     * Update the UI language.
     *
     * @param languageCode The language code to set as the UI language
     */
    suspend fun updateUiLanguage(languageCode: String) {
        preferencesManager.updateUiLanguage(languageCode)
    }
    
    /**
     * Update the first launch flag.
     *
     * @param isFirstLaunch Whether this is the first launch of the application
     */
    suspend fun updateFirstLaunch(isFirstLaunch: Boolean) {
        preferencesManager.updateFirstLaunch(isFirstLaunch)
    }
    
    /**
     * Check if this is the first launch of the application.
     *
     * @return True if this is the first launch, false otherwise
     */
    suspend fun isFirstLaunch(): Boolean {
        return preferencesManager.isFirstLaunch()
    }
    
    /**
     * Get the current UI language.
     *
     * @return The current UI language code
     */
    suspend fun getUiLanguage(): String {
        return preferencesManager.getUiLanguage()
    }
}
