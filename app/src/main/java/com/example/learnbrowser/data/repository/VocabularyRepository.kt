package com.example.learnbrowser.data.repository

import android.content.Context
import com.example.learnbrowser.data.db.VocabularyDao
import com.example.learnbrowser.data.model.VocabularyItem
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for vocabulary-related operations.
 * This class serves as a single source of truth for vocabulary data.
 */
@Singleton
class VocabularyRepository @Inject constructor(
    private val vocabularyDao: VocabularyDao
) {
    /**
     * Get all vocabulary items as a Flow.
     */
    fun getAllVocabularyItems(): Flow<List<VocabularyItem>> {
        return vocabularyDao.getAllVocabularyItems()
    }

    /**
     * Get vocabulary items for a specific source language as a Flow.
     *
     * @param sourceLanguage The source language code
     */
    fun getVocabularyItemsBySourceLanguage(sourceLanguage: String): Flow<List<VocabularyItem>> {
        return vocabularyDao.getVocabularyItemsBySourceLanguage(sourceLanguage)
    }
    
    /**
     * Get vocabulary items for multiple source languages as a Flow.
     *
     * @param sourceLanguages The list of source language codes
     */
    fun getVocabularyItemsBySourceLanguages(sourceLanguages: List<String>): Flow<List<VocabularyItem>> {
        return vocabularyDao.getVocabularyItemsBySourceLanguages(sourceLanguages)
    }

    /**
     * Insert a new vocabulary item.
     *
     * @param item The vocabulary item to insert
     * @return The row ID of the inserted item
     */
    suspend fun insertVocabularyItem(item: VocabularyItem): Long {
        return vocabularyDao.insertVocabularyItem(item)
    }

    /**
     * Delete a vocabulary item.
     *
     * @param item The vocabulary item to delete
     */
    suspend fun deleteVocabularyItem(item: VocabularyItem) {
        vocabularyDao.deleteVocabularyItem(item)
    }

    /**
     * Delete all vocabulary items.
     */
    suspend fun deleteAllVocabularyItems() {
        vocabularyDao.deleteAllVocabularyItems()
    }

    /**
     * Delete all vocabulary items for a specific source language.
     *
     * @param sourceLanguage The source language code
     */
    suspend fun deleteVocabularyItemsBySourceLanguage(sourceLanguage: String) {
        vocabularyDao.deleteVocabularyItemsBySourceLanguage(sourceLanguage)
    }

    /**
     * Get all unique source languages used in vocabulary items.
     */
    fun getAllSourceLanguages(): Flow<List<String>> {
        return vocabularyDao.getAllSourceLanguages()
    }

    /**
     * Check if a vocabulary item with the same original word and source language already exists.
     *
     * @param originalWord The original word
     * @param sourceLanguage The source language code
     * @return True if the item exists, false otherwise
     */
    suspend fun vocabularyItemExists(originalWord: String, sourceLanguage: String): Boolean {
        return vocabularyDao.vocabularyItemExists(originalWord, sourceLanguage)
    }

    /**
     * Export vocabulary items to a text file.
     *
     * @param context The application context
     * @param items The vocabulary items to export
     * @param sourceLanguage The source language (for the filename)
     * @return The exported file
     */
    suspend fun exportVocabularyItems(
        context: Context,
        items: List<VocabularyItem>,
        sourceLanguage: String
    ): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val fileName = "vocabulary_${sourceLanguage}_$timestamp.txt"
        
        val file = File(context.getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            writer.append("Original Word,Translated Word,Source Language,Target Language,Date Added\n")
            
            items.forEach { item ->
                val dateAddedFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateAdded = dateAddedFormat.format(item.dateAdded)
                
                writer.append("${item.originalWord},${item.translatedWord},${item.sourceLanguage},${item.targetLanguage},$dateAdded\n")
            }
        }
        
        return file
    }
}
