package com.example.learnbrowser.ui.vocabulary

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.data.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the VocabularyActivity.
 */
@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    // Current filter for source language
    private val _currentLanguageFilter = MutableStateFlow<String?>(null)
    
    // Vocabulary items filtered by source language
    val vocabularyItems = _currentLanguageFilter.flatMapLatest { sourceLanguage ->
        if (sourceLanguage == null) {
            vocabularyRepository.getAllVocabularyItems()
        } else {
            vocabularyRepository.getVocabularyItemsBySourceLanguage(sourceLanguage)
        }
    }.asLiveData()
    
    // All source languages
    val sourceLanguages = vocabularyRepository.getAllSourceLanguages().asLiveData()
    
    // Export result
    private val _exportResult = MutableLiveData<Pair<Boolean, File?>>()
    val exportResult: LiveData<Pair<Boolean, File?>> = _exportResult
    
    // Error handling
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Set the current language filter.
     *
     * @param sourceLanguage The source language code to filter by, or null for all languages
     */
    fun setLanguageFilter(sourceLanguage: String?) {
        _currentLanguageFilter.value = sourceLanguage
    }

    /**
     * Delete a vocabulary item.
     *
     * @param item The vocabulary item to delete
     */
    fun deleteVocabularyItem(item: VocabularyItem) {
        viewModelScope.launch {
            try {
                vocabularyRepository.deleteVocabularyItem(item)
            } catch (e: Exception) {
                _error.postValue("Failed to delete item: ${e.message}")
            }
        }
    }
    
    /**
     * Add a vocabulary item.
     *
     * @param item The vocabulary item to add
     */
    fun addVocabularyItem(item: VocabularyItem) {
        viewModelScope.launch {
            try {
                vocabularyRepository.insertVocabularyItem(item)
            } catch (e: Exception) {
                _error.postValue("Failed to add item: ${e.message}")
            }
        }
    }

    /**
     * Clear all vocabulary items.
     */
    fun clearAllVocabularyItems() {
        viewModelScope.launch {
            try {
                val sourceLanguage = _currentLanguageFilter.value
                if (sourceLanguage == null) {
                    vocabularyRepository.deleteAllVocabularyItems()
                } else {
                    vocabularyRepository.deleteVocabularyItemsBySourceLanguage(sourceLanguage)
                }
            } catch (e: Exception) {
                _error.postValue("Failed to clear vocabulary: ${e.message}")
            }
        }
    }

    /**
     * Export vocabulary items to a text file.
     *
     * @param context The application context
     */
    fun exportVocabularyItems(context: Context) {
        viewModelScope.launch {
            try {
                val items = vocabularyItems.value ?: emptyList()
                if (items.isEmpty()) {
                    _error.postValue("No vocabulary items to export")
                    return@launch
                }
                
                val sourceLanguage = _currentLanguageFilter.value ?: "all"
                val file = vocabularyRepository.exportVocabularyItems(
                    context,
                    items,
                    sourceLanguage
                )
                
                _exportResult.postValue(Pair(true, file))
            } catch (e: Exception) {
                _error.postValue("Failed to export vocabulary: ${e.message}")
                _exportResult.postValue(Pair(false, null))
            }
        }
    }
}
