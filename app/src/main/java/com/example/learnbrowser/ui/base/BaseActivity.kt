package com.example.learnbrowser.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learnbrowser.data.preferences.PreferencesManager
import com.example.learnbrowser.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base activity class that all activities should extend.
 * Handles applying the correct locale when the activity is created.
 */
@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun attachBaseContext(newBase: Context) {
        // Create a temporary PreferencesManager to get the UI language
        // This is needed because the injected preferencesManager is not yet initialized
        val tempPreferencesManager = com.example.learnbrowser.data.preferences.PreferencesManager(newBase)
        
        // Get the UI language from preferences synchronously
        val uiLanguage = runCatching {
            // Debug logging
            android.util.Log.d("BaseActivity", "About to get UI language from preferences")
            
            // Get the UI language from preferences
            val language = kotlinx.coroutines.runBlocking {
                val lang = tempPreferencesManager.getUiLanguage()
                android.util.Log.d("BaseActivity", "Got UI language from preferences: $lang")
                lang
            }
            
            // Debug logging
            android.util.Log.d("BaseActivity", "Successfully got UI language from preferences: $language")
            
            language
        }.getOrElse { e ->
            // Debug logging
            android.util.Log.e("BaseActivity", "Failed to get UI language from preferences", e)
            "en"
        }
        
        // Debug logging
        android.util.Log.d("BaseActivity", "Attaching base context with language: $uiLanguage")
        
        // Apply the locale
        val updatedContext = LocaleHelper.setLocale(newBase, uiLanguage)
        
        // Debug logging
        val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            updatedContext.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            updatedContext.resources.configuration.locale
        }
        android.util.Log.d("BaseActivity", "Current locale after setting: ${currentLocale.language}")
        
        super.attachBaseContext(updatedContext)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if RTL layout is needed
        lifecycleScope.launch {
            val uiLanguage = preferencesManager.getUiLanguage()
            if (LocaleHelper.isRtlLanguage(uiLanguage)) {
                window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
            } else {
                window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
            }
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Apply the locale again if the configuration changes
        lifecycleScope.launch {
            val uiLanguage = preferencesManager.getUiLanguage()
            LocaleHelper.setLocale(this@BaseActivity, uiLanguage)
            
            // Check if RTL layout is needed
            if (LocaleHelper.isRtlLanguage(uiLanguage)) {
                window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
            } else {
                window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
            }
        }
    }
}
