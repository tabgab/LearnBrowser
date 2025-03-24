package com.example.learnbrowser.ui.language

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnbrowser.R
import com.example.learnbrowser.databinding.ActivityLanguageSelectionBinding
import com.example.learnbrowser.databinding.ItemLanguageBinding
import com.example.learnbrowser.ui.MainActivity
import com.example.learnbrowser.ui.base.BaseActivity
import com.example.learnbrowser.util.LocaleHelper
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Activity for selecting the UI language on first launch.
 */
class LanguageSelectionActivity : BaseActivity() {
    
    private lateinit var binding: ActivityLanguageSelectionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if this is the first launch
        lifecycleScope.launch {
            if (!preferencesManager.isFirstLaunch()) {
                // If not the first launch, redirect to MainActivity
                startActivity(Intent(this@LanguageSelectionActivity, MainActivity::class.java))
                finish()
                return@launch
            }
            
            // Continue with normal setup for first launch
            binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            setupLanguageList()
        }
    }
    
    private fun setupLanguageList() {
        val languageCodes = resources.getStringArray(R.array.ui_language_codes)
        val languageNames = resources.getStringArray(R.array.ui_languages)
        
        val languages = languageCodes.zip(languageNames).map { (code, name) ->
            Language(code, name, getLocalizedLanguageName(code))
        }
        
        binding.languageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.languageRecyclerView.adapter = LanguageAdapter(languages) { language ->
            onLanguageSelected(language)
        }
    }
    
    private fun getLocalizedLanguageName(languageCode: String): String {
        val locale = Locale(languageCode)
        return locale.getDisplayLanguage(locale).capitalize(locale)
    }
    
    private fun onLanguageSelected(language: Language) {
        lifecycleScope.launch {
            // Update the UI language preference
            preferencesManager.updateUiLanguage(language.code)
            
            // Mark the app as no longer on first launch
            preferencesManager.updateFirstLaunch(false)
            
            // Start the main activity
            val intent = Intent(this@LanguageSelectionActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * Data class representing a language.
     */
    data class Language(
        val code: String,
        val name: String,
        val localizedName: String
    )
    
    /**
     * Adapter for the language list.
     */
    inner class LanguageAdapter(
        private val languages: List<Language>,
        private val onLanguageSelected: (Language) -> Unit
    ) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val language = languages[position]
            holder.bind(language)
        }
        
        override fun getItemCount(): Int = languages.size
        
        inner class ViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
            
            fun bind(language: Language) {
                binding.languageNameTextView.text = language.name
                binding.localizedNameTextView.text = language.localizedName
                
                binding.root.setOnClickListener {
                    onLanguageSelected(language)
                }
            }
        }
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(locale: Locale): String {
    return if (this.isNotEmpty()) {
        this[0].uppercaseChar() + this.substring(1)
    } else {
        this
    }
}
