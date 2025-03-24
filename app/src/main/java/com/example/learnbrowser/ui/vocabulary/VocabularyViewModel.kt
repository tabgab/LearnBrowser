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

    // Current filter for source languages
    private val _selectedLanguages = MutableStateFlow<Set<String>>(emptySet())
    
    // Vocabulary items filtered by source languages
    val vocabularyItems = _selectedLanguages.flatMapLatest { selectedLanguages ->
        if (selectedLanguages.isEmpty()) {
            vocabularyRepository.getAllVocabularyItems()
        } else {
            vocabularyRepository.getVocabularyItemsBySourceLanguages(selectedLanguages.toList())
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
     * Update the selected languages.
     *
     * @param languages The set of selected language codes
     */
    fun setSelectedLanguages(languages: Set<String>) {
        _selectedLanguages.value = languages
    }
    
    /**
     * Toggle a language selection.
     *
     * @param language The language code to toggle
     * @param selected Whether the language is selected
     */
    fun toggleLanguageSelection(language: String, selected: Boolean) {
        val currentSelection = _selectedLanguages.value.toMutableSet()
        if (selected) {
            currentSelection.add(language)
        } else {
            currentSelection.remove(language)
        }
        _selectedLanguages.value = currentSelection
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
                // Always delete all items regardless of filter
                vocabularyRepository.deleteAllVocabularyItems()
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
                
                // Use "selected" if languages are selected, otherwise "all"
                val sourceLanguage = if (_selectedLanguages.value.isEmpty()) "all" else "selected"
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
