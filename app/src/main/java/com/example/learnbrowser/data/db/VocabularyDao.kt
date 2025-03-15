package com.example.learnbrowser.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learnbrowser.data.model.VocabularyItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the vocabulary_items table.
 */
@Dao
interface VocabularyDao {
    /**
     * Insert a new vocabulary item into the database.
     * If there's a conflict, replace the existing item.
     *
     * @param item The vocabulary item to insert
     * @return The row ID of the inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabularyItem(item: VocabularyItem): Long

    /**
     * Get all vocabulary items as a Flow.
     * This will emit a new list whenever the data changes.
     *
     * @return A Flow of all vocabulary items
     */
    @Query("SELECT * FROM vocabulary_items ORDER BY dateAdded DESC")
    fun getAllVocabularyItems(): Flow<List<VocabularyItem>>

    /**
     * Get vocabulary items for a specific source language as a Flow.
     *
     * @param sourceLanguage The source language code
     * @return A Flow of vocabulary items for the specified source language
     */
    @Query("SELECT * FROM vocabulary_items WHERE sourceLanguage = :sourceLanguage ORDER BY dateAdded DESC")
    fun getVocabularyItemsBySourceLanguage(sourceLanguage: String): Flow<List<VocabularyItem>>

    /**
     * Delete a vocabulary item from the database.
     *
     * @param item The vocabulary item to delete
     */
    @Delete
    suspend fun deleteVocabularyItem(item: VocabularyItem)

    /**
     * Delete all vocabulary items from the database.
     */
    @Query("DELETE FROM vocabulary_items")
    suspend fun deleteAllVocabularyItems()

    /**
     * Delete all vocabulary items for a specific source language.
     *
     * @param sourceLanguage The source language code
     */
    @Query("DELETE FROM vocabulary_items WHERE sourceLanguage = :sourceLanguage")
    suspend fun deleteVocabularyItemsBySourceLanguage(sourceLanguage: String)

    /**
     * Get all unique source languages used in vocabulary items.
     *
     * @return A list of distinct source language codes
     */
    @Query("SELECT DISTINCT sourceLanguage FROM vocabulary_items ORDER BY sourceLanguage")
    fun getAllSourceLanguages(): Flow<List<String>>

    /**
     * Check if a vocabulary item with the same original word and source language already exists.
     *
     * @param originalWord The original word
     * @param sourceLanguage The source language code
     * @return True if the item exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM vocabulary_items WHERE originalWord = :originalWord AND sourceLanguage = :sourceLanguage LIMIT 1)")
    suspend fun vocabularyItemExists(originalWord: String, sourceLanguage: String): Boolean
}
