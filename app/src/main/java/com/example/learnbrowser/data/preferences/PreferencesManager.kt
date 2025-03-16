package com.example.learnbrowser.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.learnbrowser.BuildConfig
import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.translation.TranslationServiceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
        val TRANSLATION_SERVICE = stringPreferencesKey("translation_service")
        
        // API keys for each translation service
        val GOOGLE_TRANSLATE_API_KEY = stringPreferencesKey("google_translate_api_key")
        val LIBRE_TRANSLATE_API_KEY = stringPreferencesKey("libre_translate_api_key")
        val DEEPL_API_KEY = stringPreferencesKey("deepl_api_key")
        val MICROSOFT_TRANSLATOR_API_KEY = stringPreferencesKey("microsoft_translator_api_key")
        val YANDEX_TRANSLATE_API_KEY = stringPreferencesKey("yandex_translate_api_key")
        
        // Custom endpoints
        val LIBRE_TRANSLATE_ENDPOINT = stringPreferencesKey("libre_translate_endpoint")
    }

    // DataStore instance
    private val dataStore = context.dataStore
    
    init {
        // We'll initialize API keys lazily when they're first requested
        // This avoids the blocking call in the constructor
    }
    
    /**
     * Initialize API keys from BuildConfig if they're not already set in preferences.
     * This ensures that API keys from apikey.properties are loaded on first run.
     */
    suspend fun initializeApiKeysFromBuildConfig() {
        try {
            val preferences = dataStore.data.first()
            
            // Check if Google Translate API key is set
            if (preferences[PreferencesKeys.GOOGLE_TRANSLATE_API_KEY].isNullOrEmpty() && 
                BuildConfig.GOOGLE_TRANSLATE_API_KEY.isNotEmpty()) {
                updateTranslationApiKey(
                    TranslationServiceType.GOOGLE_TRANSLATE, 
                    BuildConfig.GOOGLE_TRANSLATE_API_KEY
                )
            }
            
            // Check if LibreTranslate API key is set
            if (preferences[PreferencesKeys.LIBRE_TRANSLATE_API_KEY].isNullOrEmpty() && 
                BuildConfig.LIBRE_TRANSLATE_API_KEY.isNotEmpty()) {
                updateTranslationApiKey(
                    TranslationServiceType.LIBRE_TRANSLATE, 
                    BuildConfig.LIBRE_TRANSLATE_API_KEY
                )
            }
            
            // Check if LibreTranslate endpoint is set
            if (preferences[PreferencesKeys.LIBRE_TRANSLATE_ENDPOINT].isNullOrEmpty() && 
                BuildConfig.LIBRE_TRANSLATE_ENDPOINT.isNotEmpty()) {
                updateCustomEndpoint(
                    TranslationServiceType.LIBRE_TRANSLATE, 
                    BuildConfig.LIBRE_TRANSLATE_ENDPOINT
                )
            }
            
            // Check if DeepL API key is set
            if (preferences[PreferencesKeys.DEEPL_API_KEY].isNullOrEmpty() && 
                BuildConfig.DEEPL_API_KEY.isNotEmpty()) {
                updateTranslationApiKey(
                    TranslationServiceType.DEEPL, 
                    BuildConfig.DEEPL_API_KEY
                )
            }
            
            // Check if Microsoft Translator API key is set
            if (preferences[PreferencesKeys.MICROSOFT_TRANSLATOR_API_KEY].isNullOrEmpty() && 
                BuildConfig.MICROSOFT_TRANSLATOR_API_KEY.isNotEmpty()) {
                updateTranslationApiKey(
                    TranslationServiceType.MICROSOFT_TRANSLATOR, 
                    BuildConfig.MICROSOFT_TRANSLATOR_API_KEY
                )
            }
            
            // Check if Yandex Translate API key is set
            if (preferences[PreferencesKeys.YANDEX_TRANSLATE_API_KEY].isNullOrEmpty() && 
                BuildConfig.YANDEX_TRANSLATE_API_KEY.isNotEmpty()) {
                updateTranslationApiKey(
                    TranslationServiceType.YANDEX_TRANSLATE, 
                    BuildConfig.YANDEX_TRANSLATE_API_KEY
                )
            }
        } catch (e: Exception) {
            // Log the error but don't crash the app
            e.printStackTrace()
        }
    }

    /**
     * Get the user settings as a Flow.
     * This will emit a new Settings object whenever the preferences change.
     */
    val userSettingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        val translationService = TranslationServiceType.fromName(
            preferences[PreferencesKeys.TRANSLATION_SERVICE] ?: TranslationServiceType.getDefault().name
        )
        
        val translationApiKeys = mutableMapOf<TranslationServiceType, String>()
        translationApiKeys[TranslationServiceType.GOOGLE_TRANSLATE] = preferences[PreferencesKeys.GOOGLE_TRANSLATE_API_KEY] ?: ""
        translationApiKeys[TranslationServiceType.LIBRE_TRANSLATE] = preferences[PreferencesKeys.LIBRE_TRANSLATE_API_KEY] ?: ""
        translationApiKeys[TranslationServiceType.DEEPL] = preferences[PreferencesKeys.DEEPL_API_KEY] ?: ""
        translationApiKeys[TranslationServiceType.MICROSOFT_TRANSLATOR] = preferences[PreferencesKeys.MICROSOFT_TRANSLATOR_API_KEY] ?: ""
        translationApiKeys[TranslationServiceType.YANDEX_TRANSLATE] = preferences[PreferencesKeys.YANDEX_TRANSLATE_API_KEY] ?: ""
        
        val customEndpoints = mutableMapOf<TranslationServiceType, String>()
        customEndpoints[TranslationServiceType.LIBRE_TRANSLATE] = preferences[PreferencesKeys.LIBRE_TRANSLATE_ENDPOINT] ?: "https://libretranslate.com/translate"
        
        Settings(
            targetLanguage = preferences[PreferencesKeys.TARGET_LANGUAGE] ?: "en",
            autoTranslatePages = preferences[PreferencesKeys.AUTO_TRANSLATE_PAGES] ?: false,
            downloadedLanguages = preferences[PreferencesKeys.DOWNLOADED_LANGUAGES]?.toList() ?: emptyList(),
            translationService = translationService,
            translationApiKeys = translationApiKeys,
            customEndpoints = customEndpoints
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
     * Update the translation service preference.
     *
     * @param serviceType The translation service to use
     */
    suspend fun updateTranslationService(serviceType: TranslationServiceType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRANSLATION_SERVICE] = serviceType.name
        }
    }

    /**
     * Update an API key for a translation service.
     *
     * @param serviceType The translation service
     * @param apiKey The API key
     */
    suspend fun updateTranslationApiKey(serviceType: TranslationServiceType, apiKey: String) {
        dataStore.edit { preferences ->
            when (serviceType) {
                TranslationServiceType.GOOGLE_TRANSLATE -> preferences[PreferencesKeys.GOOGLE_TRANSLATE_API_KEY] = apiKey
                TranslationServiceType.LIBRE_TRANSLATE -> preferences[PreferencesKeys.LIBRE_TRANSLATE_API_KEY] = apiKey
                TranslationServiceType.DEEPL -> preferences[PreferencesKeys.DEEPL_API_KEY] = apiKey
                TranslationServiceType.MICROSOFT_TRANSLATOR -> preferences[PreferencesKeys.MICROSOFT_TRANSLATOR_API_KEY] = apiKey
                TranslationServiceType.YANDEX_TRANSLATE -> preferences[PreferencesKeys.YANDEX_TRANSLATE_API_KEY] = apiKey
                else -> { /* No API key needed */ }
            }
        }
    }

    /**
     * Update a custom endpoint for a translation service.
     *
     * @param serviceType The translation service
     * @param endpoint The custom endpoint
     */
    suspend fun updateCustomEndpoint(serviceType: TranslationServiceType, endpoint: String) {
        dataStore.edit { preferences ->
            when (serviceType) {
                TranslationServiceType.LIBRE_TRANSLATE -> preferences[PreferencesKeys.LIBRE_TRANSLATE_ENDPOINT] = endpoint
                else -> { /* No custom endpoint supported */ }
            }
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
