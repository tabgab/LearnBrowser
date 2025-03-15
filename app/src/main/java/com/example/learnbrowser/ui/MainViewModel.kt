package com.example.learnbrowser.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnbrowser.data.model.Settings
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.data.repository.SettingsRepository
import com.example.learnbrowser.data.repository.VocabularyRepository
import com.example.learnbrowser.data.translation.TranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the MainActivity.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
    private val translationService: TranslationService
) : ViewModel() {

    // Error handling
    private val _translationError = MutableLiveData<String>()
    val translationError: LiveData<String> = _translationError

    /**
     * Get the current user settings.
     */
    suspend fun getSettings(): Settings {
        return settingsRepository.userSettings.first()
    }

    /**
     * Translate text from one language to another.
     *
     * @param text The text to translate
     * @param sourceLanguage The source language code
     * @param targetLanguage The target language code
     * @return The translated text
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        return try {
            translationService.translateText(text, sourceLanguage, targetLanguage)
        } catch (e: Exception) {
            _translationError.postValue("Translation failed: ${e.message}")
            text
        }
    }

    /**
     * Detect the language of a text.
     *
     * @param text The text to detect the language of
     * @return The detected language code
     */
    suspend fun detectLanguage(text: String): String {
        return try {
            translationService.detectLanguage(text)
        } catch (e: Exception) {
            _translationError.postValue("Language detection failed: ${e.message}")
            "en" // Default to English if detection fails
        }
    }

    /**
     * Add a vocabulary item to the database.
     *
     * @param item The vocabulary item to add
     */
    suspend fun addVocabularyItem(item: VocabularyItem) {
        try {
            // Check if the item already exists
            val exists = vocabularyRepository.vocabularyItemExists(
                item.originalWord,
                item.sourceLanguage
            )
            
            if (!exists) {
                vocabularyRepository.insertVocabularyItem(item)
            } else {
                _translationError.postValue("Word already exists in vocabulary")
            }
        } catch (e: Exception) {
            _translationError.postValue("Failed to add to vocabulary: ${e.message}")
        }
    }
}
