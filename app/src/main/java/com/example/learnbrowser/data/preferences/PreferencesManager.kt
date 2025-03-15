package com.example.learnbrowser.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.learnbrowser.data.model.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for Context to create a DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manager class for handling user preferences using DataStore.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Preference keys
    private object PreferencesKeys {
        val TARGET_LANGUAGE = stringPreferencesKey("target_language")
        val AUTO_TRANSLATE_PAGES = booleanPreferencesKey("auto_translate_pages")
        val DOWNLOADED_LANGUAGES = stringSetPreferencesKey("downloaded_languages")
    }

    // DataStore instance
    private val dataStore = context.dataStore

    /**
     * Get the user settings as a Flow.
     * This will emit a new Settings object whenever the preferences change.
     */
    val userSettingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            targetLanguage = preferences[PreferencesKeys.TARGET_LANGUAGE] ?: "en",
            autoTranslatePages = preferences[PreferencesKeys.AUTO_TRANSLATE_PAGES] ?: false,
            downloadedLanguages = preferences[PreferencesKeys.DOWNLOADED_LANGUAGES]?.toList() ?: emptyList()
        )
    }

    /**
     * Update the target language preference.
     *
     * @param languageCode The language code to set as the target language
     */
    suspend fun updateTargetLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_LANGUAGE] = languageCode
        }
    }

    /**
     * Update the auto-translate pages preference.
     *
     * @param autoTranslate Whether to auto-translate pages
     */
    suspend fun updateAutoTranslatePages(autoTranslate: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_TRANSLATE_PAGES] = autoTranslate
        }
    }

    /**
     * Add a language to the downloaded languages list.
     *
     * @param languageCode The language code to add
     */
    suspend fun addDownloadedLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            val currentLanguages = preferences[PreferencesKeys.DOWNLOADED_LANGUAGES] ?: emptySet()
            preferences[PreferencesKeys.DOWNLOADED_LANGUAGES] = currentLanguages + languageCode
        }
    }

    /**
     * Remove a language from the downloaded languages list.
     *
     * @param languageCode The language code to remove
     */
    suspend fun removeDownloadedLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            val currentLanguages = preferences[PreferencesKeys.DOWNLOADED_LANGUAGES] ?: emptySet()
            preferences[PreferencesKeys.DOWNLOADED_LANGUAGES] = currentLanguages - languageCode
        }
    }

    /**
     * Clear all downloaded languages.
     */
    suspend fun clearDownloadedLanguages() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOADED_LANGUAGES] = emptySet()
        }
    }
}
