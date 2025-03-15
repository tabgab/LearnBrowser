package com.example.learnbrowser.ui

import android.content.Context
import com.example.learnbrowser.R

/**
 * Get the human-readable name of a language from its code.
 *
 * @param languageCode The language code (e.g., "en" for English)
 * @return The language name
 */
fun Context.getLanguageName(languageCode: String): String {
    val languageCodes = resources.getStringArray(R.array.language_codes)
    val languageNames = resources.getStringArray(R.array.languages)
    
    val index = languageCodes.indexOf(languageCode)
    return if (index != -1) {
        languageNames[index]
    } else {
        languageCode
    }
}
