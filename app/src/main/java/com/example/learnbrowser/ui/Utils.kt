package com.example.learnbrowser.ui

import android.content.Context
import com.example.learnbrowser.R
import java.util.Locale

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
        // Use predefined name if available
        languageNames[index]
    } else {
            // Use Java's Locale to get the display name for the language in the current UI locale
            try {
                val locale = Locale(languageCode)
                // Get the current locale from the context's resources
                val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    resources.configuration.locale
                }
                
                val displayName = locale.getDisplayLanguage(currentLocale)
                
                // If the display name is the same as the code, it means the locale wasn't recognized
                if (displayName.equals(languageCode, ignoreCase = true)) {
                    // Try to see if it's a country code or a language+country code
                    if (languageCode.contains("-") || languageCode.contains("_")) {
                        val parts = languageCode.replace("_", "-").split("-")
                        if (parts.size >= 2) {
                            val langCode = parts[0]
                            val countryCode = parts[1].uppercase()
                            val langLocale = Locale(langCode)
                            val langName = langLocale.getDisplayLanguage(currentLocale)
                            if (!langName.equals(langCode, ignoreCase = true)) {
                                return "$langName ($countryCode)"
                            }
                        }
                    }
                // Fall back to the code if we couldn't get a proper name
                languageCode
            } else {
                // Capitalize the first letter
                displayName.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                }
            }
        } catch (e: Exception) {
            // If there's any error, fall back to the code
            languageCode
        }
    }
}
