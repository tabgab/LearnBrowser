package com.example.learnbrowser.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.learnbrowser.R
import com.example.learnbrowser.databinding.ActivitySettingsBinding
import com.example.learnbrowser.ui.getLanguageName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupTargetLanguage()
        setupAutoTranslate()
        setupDownloadLanguage()
        setupSaveButton()
        setupObservers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupTargetLanguage() {
        lifecycleScope.launch {
            // Get supported languages
            val languageCodes = viewModel.getSupportedLanguages()
            val languageNames = languageCodes.map { getLanguageName(it) }
            
            // Create adapter
            val adapter = ArrayAdapter(
                this@SettingsActivity,
                android.R.layout.simple_dropdown_item_1line,
                languageNames
            )
            
            binding.targetLanguageAutoComplete.setAdapter(adapter)
            
            // Set current selection
            viewModel.settings.observe(this@SettingsActivity) { settings ->
                val currentLanguageCode = settings.targetLanguage
                val currentLanguageName = getLanguageName(currentLanguageCode)
                binding.targetLanguageAutoComplete.setText(currentLanguageName, false)
            }
            
            // Handle selection
            binding.targetLanguageAutoComplete.setOnItemClickListener { _, _, position, _ ->
                val selectedLanguageCode = languageCodes[position]
                viewModel.updateTargetLanguage(selectedLanguageCode)
            }
        }
    }
    
    private fun setupAutoTranslate() {
        // Set current state
        viewModel.settings.observe(this) { settings ->
            binding.autoTranslateSwitch.isChecked = settings.autoTranslatePages
        }
        
        // Handle changes
        binding.autoTranslateSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAutoTranslatePages(isChecked)
        }
    }
    
    private fun setupDownloadLanguage() {
        lifecycleScope.launch {
            // Get supported languages
            val languageCodes = viewModel.getSupportedLanguages()
            val languageNames = languageCodes.map { getLanguageName(it) }
            
            // Create adapter
            val adapter = ArrayAdapter(
                this@SettingsActivity,
                android.R.layout.simple_dropdown_item_1line,
                languageNames
            )
            
            binding.downloadLanguageAutoComplete.setAdapter(adapter)
            
            // Handle selection
            binding.downloadLanguageAutoComplete.setOnItemClickListener { _, _, position, _ ->
                binding.downloadButton.isEnabled = true
                binding.downloadButton.tag = languageCodes[position]
            }
            
            // Handle download button
            binding.downloadButton.setOnClickListener {
                val languageCode = it.tag as? String
                if (languageCode != null) {
                    lifecycleScope.launch {
                        // Check if already downloaded
                        val isDownloaded = viewModel.isLanguageDownloaded(languageCode)
                        if (isDownloaded) {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Language already downloaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Start download
                            binding.downloadButton.isEnabled = false
                            viewModel.downloadLanguage(languageCode)
                        }
                    }
                }
            }
        }
        
        // Initially disable download button
        binding.downloadButton.isEnabled = false
    }
    
    private fun setupSaveButton() {
        binding.saveSettingsButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupObservers() {
        // Observe errors
        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe language download state
        viewModel.languageDownloadState.observe(this) { (languageCode, success) ->
            binding.downloadButton.isEnabled = true
            
            if (success) {
                Toast.makeText(
                    this,
                    getString(R.string.language_downloaded),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.language_download_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
