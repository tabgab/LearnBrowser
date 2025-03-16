package com.example.learnbrowser.data.translation

import android.content.Context
import com.example.learnbrowser.BuildConfig
import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling translation operations using various translation services.
 */
@Singleton
class TranslationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : TranslationServiceInterface {

    private suspend fun getTranslationService(): TranslationServiceInterface {
        val settings = settingsRepository.userSettings.first()
        
        return when (settings.translationService) {
            TranslationServiceType.GOOGLE_TRANSLATE -> {
                val apiKey = settings.translationApiKeys[TranslationServiceType.GOOGLE_TRANSLATE] ?: BuildConfig.TRANSLATE_API_KEY
                GoogleTranslateService(context, apiKey)
            }
            TranslationServiceType.LIBRE_TRANSLATE -> {
                val apiKey = settings.translationApiKeys[TranslationServiceType.LIBRE_TRANSLATE] ?: ""
                val endpoint = settings.customEndpoints[TranslationServiceType.LIBRE_TRANSLATE] ?: "https://libretranslate.com/translate"
                LibreTranslateService(context, apiKey, endpoint)
            }
            TranslationServiceType.DEEPL -> {
                val apiKey = settings.translationApiKeys[TranslationServiceType.DEEPL] ?: ""
                DeepLTranslateService(context, apiKey)
            }
            TranslationServiceType.LINGVA_TRANSLATE -> {
                LingvaTranslateService(context)
            }
            TranslationServiceType.ARGOS_TRANSLATE -> {
                ArgosTranslateService(context)
            }
            TranslationServiceType.MICROSOFT_TRANSLATOR -> {
                val apiKey = settings.translationApiKeys[TranslationServiceType.MICROSOFT_TRANSLATOR] ?: ""
                MicrosoftTranslateService(context, apiKey)
            }
            TranslationServiceType.YANDEX_TRANSLATE -> {
                val apiKey = settings.translationApiKeys[TranslationServiceType.YANDEX_TRANSLATE] ?: ""
                YandexTranslateService(context, apiKey)
            }
        }
    }

    override suspend fun translateText(
        text: String,
        sourceLanguage: String?,
        targetLanguage: String
    ): String {
        return getTranslationService().translateText(text, sourceLanguage, targetLanguage)
    }

    override suspend fun detectLanguage(text: String): String {
        return getTranslationService().detectLanguage(text)
    }

    override suspend fun getSupportedLanguages(): List<String> {
        return getTranslationService().getSupportedLanguages()
    }

    override suspend fun downloadLanguageModel(languageCode: String): Boolean {
        return getTranslationService().downloadLanguageModel(languageCode)
    }

    /**
     * Check if offline translation is available for a specific language.
     *
     * @param languageCode The language code to check
     * @param settings The current user settings
     * @return True if offline translation is available, false otherwise
     */
    fun isOfflineTranslationAvailable(languageCode: String, settings: Settings): Boolean {
        return settings.downloadedLanguages.contains(languageCode)
    }
}
