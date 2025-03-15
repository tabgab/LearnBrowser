package com.example.learnbrowser.data.repository

import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for settings-related operations.
 * This class serves as a single source of truth for user settings.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val preferencesManager: PreferencesManager
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
}
