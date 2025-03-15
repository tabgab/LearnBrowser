package com.example.learnbrowser.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a vocabulary item.
 * This is used as a Room entity for database storage.
 */
@Entity(tableName = "vocabulary_items")
data class VocabularyItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * The original word in the source language
     */
    val originalWord: String,
    
    /**
     * The translated word in the target language
     */
    val translatedWord: String,
    
    /**
     * The source language code (e.g., "en" for English)
     */
    val sourceLanguage: String,
    
    /**
     * The target language code (e.g., "es" for Spanish)
     */
    val targetLanguage: String,
    
    /**
     * The date when this vocabulary item was added
     */
    val dateAdded: Date = Date()
)
