package com.example.learnbrowser.ui.settings

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.learnbrowser.R
import com.example.learnbrowser.data.translation.TranslationServiceType
import com.example.learnbrowser.databinding.ActivitySettingsBinding
import com.example.learnbrowser.databinding.DialogApiKeyInstructionsBinding
import com.example.learnbrowser.ui.MainActivity
import com.example.learnbrowser.ui.base.BaseActivity
import com.example.learnbrowser.ui.getLanguageName
import com.example.learnbrowser.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    
    // Track if UI language has changed
    private var uiLanguageChanged = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupTranslationService()
        setupUiLanguage()
        setupTargetLanguage()
        setupAutoTranslate()
        setupDownloadLanguage()
        setupSaveButton()
        setupApiKeyHelpButton()
        setupObservers()
    }
    
    /**
     * Set up the "How to get API key" button.
     */
    private fun setupApiKeyHelpButton() {
        binding.getApiKeyHelpButton.setOnClickListener {
            // Get the current translation service
            val currentService = viewModel.settings.value?.translationService ?: TranslationServiceType.getDefault()
            
            // Show the API key instructions dialog
            showApiKeyInstructionsDialog(currentService)
        }
    }
    
    /**
     * Show a dialog with instructions on how to get an API key for the specified translation service.
     *
     * @param serviceType The translation service type
     */
    private fun showApiKeyInstructionsDialog(serviceType: TranslationServiceType) {
        // Create a dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_api_key_instructions)
        
        // Set the dialog to be full width
        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set the dialog title
        val titleTextView = dialog.findViewById<TextView>(R.id.dialogTitle)
        titleTextView.text = getString(R.string.api_key_instructions_title, serviceType.displayName)
        
        // Get the instructions for the selected service
        val instructionsResId = when (serviceType) {
            TranslationServiceType.GOOGLE_TRANSLATE -> R.string.api_key_instructions_google
            TranslationServiceType.LIBRE_TRANSLATE -> R.string.api_key_instructions_libre
            TranslationServiceType.DEEPL -> R.string.api_key_instructions_deepl
            TranslationServiceType.MICROSOFT_TRANSLATOR -> R.string.api_key_instructions_microsoft
            TranslationServiceType.YANDEX_TRANSLATE -> R.string.api_key_instructions_yandex
            TranslationServiceType.LINGVA_TRANSLATE -> R.string.api_key_instructions_lingva
            TranslationServiceType.ARGOS_TRANSLATE -> R.string.api_key_instructions_argos
        }
        
        // Set up the WebView with the instructions
        val webView = dialog.findViewById<WebView>(R.id.instructionsWebView)
        
        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        
        // Enable links to be clickable
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // Open links in external browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }
        })
        
        // Add CSS styling to make the content more readable
        val cssStyle = """
            <style>
                body {
                    font-family: 'Roboto', sans-serif;
                    line-height: 1.8;
                    color: #333;
                    padding: 16px;
                    font-size: 22px;
                }
                h2 {
                    color: #1976D2;
                    margin-top: 0;
                    font-size: 26px;
                    margin-bottom: 20px;
                }
                p {
                    margin-bottom: 20px;
                    font-size: 22px;
                }
                ol {
                    padding-left: 28px;
                    margin-bottom: 20px;
                }
                li {
                    margin-bottom: 16px;
                    font-size: 22px;
                }
                a {
                    color: #2196F3;
                    text-decoration: none;
                    font-weight: bold;
                }
            </style>
        """
        
        // Combine CSS with the HTML content
        val htmlContent = "<html><head>$cssStyle</head><body>${getString(instructionsResId)}</body></html>"
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        
        // Set up the OK button
        val okButton = dialog.findViewById<Button>(R.id.okButton)
        okButton.setOnClickListener {
            dialog.dismiss()
        }
        
        // Show the dialog
        dialog.show()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupTranslationService() {
        // Get available translation services
        val translationServices = viewModel.getAvailableTranslationServices()
        val serviceNames = translationServices.map { it.displayName }
        
        // Create adapter
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            serviceNames
        )
        
        binding.translationServiceAutoComplete.setAdapter(adapter)
        
        // Set current selection
        viewModel.settings.observe(this) { settings ->
            val currentService = settings.translationService
            val currentServiceName = currentService.displayName
            binding.translationServiceAutoComplete.setText(currentServiceName, false)
            
            // Update API key field
            lifecycleScope.launch {
                val apiKey = viewModel.getTranslationApiKey(currentService)
                binding.apiKeyEditText.setText(apiKey)
                
                // Show/hide API key field based on whether the service requires an API key
                if (currentService.requiresApiKey) {
                    binding.apiKeyLayout.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.text = getString(R.string.api_key_required)
                } else {
                    binding.apiKeyLayout.visibility = View.GONE
                    binding.apiKeyRequiredTextView.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.text = getString(R.string.api_key_not_required)
                }
                
                // Show/hide custom endpoint field based on whether the service supports it
                if (currentService == TranslationServiceType.LIBRE_TRANSLATE) {
                    binding.customEndpointLayout.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.text = getString(R.string.custom_endpoint_supported)
                    
                    // Set current custom endpoint
                    val endpoint = viewModel.getCustomEndpoint(currentService)
                    binding.customEndpointEditText.setText(endpoint)
                } else {
                    binding.customEndpointLayout.visibility = View.GONE
                    binding.customEndpointSupportedTextView.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.text = getString(R.string.custom_endpoint_not_supported)
                }
            }
        }
        
        // Handle selection
        binding.translationServiceAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedService = translationServices[position]
            viewModel.updateTranslationService(selectedService)
            
            // Update API key field
            lifecycleScope.launch {
                val apiKey = viewModel.getTranslationApiKey(selectedService)
                binding.apiKeyEditText.setText(apiKey)
                
                // Show/hide API key field based on whether the service requires an API key
                if (selectedService.requiresApiKey) {
                    binding.apiKeyLayout.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.text = getString(R.string.api_key_required)
                } else {
                    binding.apiKeyLayout.visibility = View.GONE
                    binding.apiKeyRequiredTextView.visibility = View.VISIBLE
                    binding.apiKeyRequiredTextView.text = getString(R.string.api_key_not_required)
                }
                
                // Show/hide custom endpoint field based on whether the service supports it
                if (selectedService == TranslationServiceType.LIBRE_TRANSLATE) {
                    binding.customEndpointLayout.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.text = getString(R.string.custom_endpoint_supported)
                    
                    // Set current custom endpoint
                    val endpoint = viewModel.getCustomEndpoint(selectedService)
                    binding.customEndpointEditText.setText(endpoint)
                } else {
                    binding.customEndpointLayout.visibility = View.GONE
                    binding.customEndpointSupportedTextView.visibility = View.VISIBLE
                    binding.customEndpointSupportedTextView.text = getString(R.string.custom_endpoint_not_supported)
                }
                
                // Update target language dropdown with languages supported by the selected service
                updateTargetLanguageDropdown()
            }
        }
    }
    
    private fun setupTargetLanguage() {
        // Set up the target language dropdown
        updateTargetLanguageDropdown()
        
        // Set current selection
        viewModel.settings.observe(this) { settings ->
            val currentLanguageCode = settings.targetLanguage
            val currentLanguageName = getLanguageName(currentLanguageCode)
            binding.targetLanguageAutoComplete.setText(currentLanguageName, false)
        }
    }
    
    /**
     * Update the target language dropdown with languages supported by the current translation service.
     */
    private fun updateTargetLanguageDropdown() {
        lifecycleScope.launch {
            // Get current translation service
            val currentService = viewModel.settings.value?.translationService ?: TranslationServiceType.getDefault()
            
            // Get supported languages for the current service
            val languageCodes = viewModel.getSupportedLanguagesForService(currentService)
            val languageNames = languageCodes.map { getLanguageName(it) }
            
            // Create adapter
            val adapter = ArrayAdapter(
                this@SettingsActivity,
                android.R.layout.simple_dropdown_item_1line,
                languageNames
            )
            
            binding.targetLanguageAutoComplete.setAdapter(adapter)
            
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
        // Set up the download language dropdown
        updateDownloadLanguageDropdown()
        
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
        
        // Initially disable download button
        binding.downloadButton.isEnabled = false
        
        // Update download language dropdown when translation service changes
        viewModel.settings.observe(this) { settings ->
            lifecycleScope.launch {
                updateDownloadLanguageDropdown()
            }
        }
    }
    
    /**
     * Update the download language dropdown with languages supported by the current translation service.
     */
    private fun updateDownloadLanguageDropdown() {
        lifecycleScope.launch {
            // Get current translation service
            val currentService = viewModel.settings.value?.translationService ?: TranslationServiceType.getDefault()
            
            // Get supported languages for the current service
            val languageCodes = viewModel.getSupportedLanguagesForService(currentService)
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
        }
    }
    
    private fun setupUiLanguage() {
        // Get available UI languages
        val uiLanguages = viewModel.getAvailableUiLanguages(this)
        val languageNames = uiLanguages.map { it.second }
        
        // Create adapter
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            languageNames
        )
        
        binding.uiLanguageAutoComplete.setAdapter(adapter)
        
        // Set current selection
        lifecycleScope.launch {
            val currentLanguageCode = viewModel.getUiLanguage()
            val currentLanguageName = uiLanguages.find { it.first == currentLanguageCode }?.second
                ?: uiLanguages.first().second
            
            binding.uiLanguageAutoComplete.setText(currentLanguageName, false)
        }
        
        // Handle selection
        binding.uiLanguageAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguageCode = uiLanguages[position].first
            
            // Debug logging
            android.util.Log.d("SettingsActivity", "Selected UI language: $selectedLanguageCode")
            
            viewModel.updateUiLanguage(selectedLanguageCode)
            
            // Apply the language change immediately
            applyLanguageChange(selectedLanguageCode)
        }
    }
    
    /**
     * Apply the language change immediately by recreating all activities.
     *
     * @param languageCode The language code to apply
     */
    private fun applyLanguageChange(languageCode: String) {
        // Apply the locale to the current activity
        val context = LocaleHelper.setLocale(this, languageCode)
        resources.updateConfiguration(context.resources.configuration, resources.displayMetrics)
        
        // Recreate all activities to apply the language change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        
        // Show a toast message
        Toast.makeText(
            this,
            "Language changed to ${getLanguageName(languageCode)}",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun setupSaveButton() {
        binding.saveSettingsButton.setOnClickListener {
            // Save API key if visible
            if (binding.apiKeyLayout.visibility == View.VISIBLE) {
                val selectedService = viewModel.settings.value?.translationService ?: TranslationServiceType.getDefault()
                val apiKey = binding.apiKeyEditText.text.toString()
                viewModel.updateTranslationApiKey(selectedService, apiKey)
            }
            
            // Save custom endpoint if visible
            if (binding.customEndpointLayout.visibility == View.VISIBLE) {
                val selectedService = viewModel.settings.value?.translationService ?: TranslationServiceType.getDefault()
                val endpoint = binding.customEndpointEditText.text.toString()
                viewModel.updateCustomEndpoint(selectedService, endpoint)
            }
            
            // If UI language has changed, ask if the user wants to restart the app
            if (uiLanguageChanged) {
                showRestartDialog()
            } else {
                finish()
            }
        }
    }
    
    /**
     * Show a dialog asking the user if they want to restart the app to apply the UI language change.
     */
    private fun showRestartDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.ui_language_setting)
            .setMessage(R.string.ui_language_restart_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                // Restart the app
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ ->
                // Just finish the activity
                finish()
            }
            .show()
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
