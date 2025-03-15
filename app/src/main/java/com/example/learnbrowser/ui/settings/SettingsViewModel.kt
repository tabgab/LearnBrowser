package com.example.learnbrowser.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.repository.SettingsRepository
import com.example.learnbrowser.data.translation.TranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the SettingsActivity.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val translationService: TranslationService
) : ViewModel() {

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
     * Get a list of supported languages.
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
}
