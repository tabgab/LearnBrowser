package com.example.learnbrowser.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper class for handling locale changes at runtime.
 */
object LocaleHelper {
    /**
     * Set the app's locale to the specified language code.
     *
     * @param context The context to update
     * @param languageCode The language code to set (e.g., "en", "fr", "de")
     * @return The updated context with the new locale applied
     */
    fun setLocale(context: Context, languageCode: String): Context {
        android.util.Log.d("LocaleHelper", "Setting locale to: $languageCode")
        
        updateLocale(context, languageCode)
        
        // For API 25 and above, we need to create a new context with the updated configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val config = Configuration(context.resources.configuration)
            config.setLocale(Locale(languageCode))
            val updatedContext = context.createConfigurationContext(config)
            
            // Debug logging
            val currentLocale = updatedContext.resources.configuration.locales[0]
            android.util.Log.d("LocaleHelper", "Current locale after setting (API 25+): ${currentLocale.language}")
            
            updatedContext
        } else {
            // For older API levels, we update the configuration directly
            // Debug logging
            @Suppress("DEPRECATION")
            val currentLocale = context.resources.configuration.locale
            android.util.Log.d("LocaleHelper", "Current locale after setting (API < 25): ${currentLocale.language}")
            
            context
        }
    }
    
    /**
     * Update the locale of the given context.
     *
     * @param context The context to update
     * @param languageCode The language code to set
     */
    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    /**
     * Get the current locale from the context.
     *
     * @param context The context to get the locale from
     * @return The current locale's language code
     */
    fun getLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
    }
    
    /**
     * Check if the given language code requires Right-to-Left layout.
     *
     * @param languageCode The language code to check
     * @return True if the language is RTL, false otherwise
     */
    fun isRtlLanguage(languageCode: String): Boolean {
        return when (languageCode) {
            "ar", "ur" -> true // Arabic, Urdu
            else -> false
        }
    }
}
