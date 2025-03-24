package com.example.learnbrowser.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.learnbrowser.R
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.databinding.ActivityMainBinding
import com.example.learnbrowser.ui.base.BaseActivity
import com.example.learnbrowser.ui.language.LanguageSelectionActivity
import com.example.learnbrowser.ui.settings.SettingsActivity
import com.example.learnbrowser.ui.translation.TranslationDialogFragment
import com.example.learnbrowser.ui.vocabulary.VocabularyActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity(), TranslationDialogFragment.TranslationDialogListener {

    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
    
    // Selected text for translation
    private var selectedText: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if this is the first launch
        lifecycleScope.launch {
            if (viewModel.isFirstLaunch()) {
                // If this is the first launch, redirect to LanguageSelectionActivity
                startActivity(Intent(this@MainActivity, LanguageSelectionActivity::class.java))
                finish()
                return@launch
            }
            
            // Continue with normal setup
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            setupToolbar()
            setupWebView()
            setupNavigation()
            setupObservers()
            
            // Load a default page
            binding.urlEditText.setText("https://www.google.com")
            loadUrl("https://www.google.com")
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupWebView() {
        // Enable JavaScript and DOM storage
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
        }
        
        // Set up custom long press handler
        binding.webView.setOnLongClickListener { view ->
            // Get selected text via JavaScript with a delay to ensure selection is complete
            Handler(Looper.getMainLooper()).postDelayed({
                binding.webView.evaluateJavascript(
                    "(function() { return window.getSelection().toString(); })();",
                    { value ->
                        // Remove quotes from the returned value
                        val text = value.replace("\"", "").trim()
                        
                        if (text.isNotEmpty()) {
                            // Show translation dialog directly
                            runOnUiThread {
                                showTranslationDialog(text)
                            }
                        } else {
                            // If no text is selected, show a message
                            runOnUiThread {
                                Toast.makeText(this, "No text selected. Try selecting text first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }, 300) // 300ms delay to allow selection to complete
            
            false // Don't consume the event to allow the default selection behavior
        }
        
        // Set WebViewClient to handle page navigation
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.urlEditText.setText(url)
                
                // Inject JavaScript to handle text selection
                view?.evaluateJavascript(
                    """
                    (function() {
                        document.addEventListener('selectionchange', function() {
                            var selection = window.getSelection();
                            if (selection.toString().length > 0) {
                                console.log('Text selected: ' + selection.toString());
                            }
                        });
                    })();
                    """.trimIndent(),
                    null
                )
                
                // Check if auto-translate is enabled
                lifecycleScope.launch {
                    val settings = viewModel.getSettings()
                    if (settings.autoTranslatePages) {
                        translatePage()
                    }
                }
            }
        }
        
        // Set WebChromeClient to handle progress updates
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    
    private fun setupNavigation() {
        // URL input and navigation
        binding.goButton.setOnClickListener {
            val url = binding.urlEditText.text.toString()
            loadUrl(url)
        }
        
        binding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                val url = binding.urlEditText.text.toString()
                loadUrl(url)
                true
            } else {
                false
            }
        }
        
        // Navigation buttons
        binding.backButton.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            }
        }
        
        binding.forwardButton.setOnClickListener {
            if (binding.webView.canGoForward()) {
                binding.webView.goForward()
            }
        }
        
        binding.refreshButton.setOnClickListener {
            binding.webView.reload()
        }
        
        // Translate page button
        binding.translatePageButton.setOnClickListener {
            translatePage()
        }
        
        // Vocabulary button
        binding.vocabularyButton.setOnClickListener {
            val intent = Intent(this, VocabularyActivity::class.java)
            startActivity(intent)
        }
        
        // Settings button
        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupObservers() {
        // Observe translation errors
        viewModel.translationError.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadUrl(url: String) {
        var processedUrl = url
        
        // Add http:// prefix if not present
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            processedUrl = "https://$url"
        }
        
        binding.webView.loadUrl(processedUrl)
    }
    
    private fun translatePage() {
        lifecycleScope.launch {
            try {
                val settings = viewModel.getSettings()
                val targetLanguage = settings.targetLanguage
                
                // Show a toast to indicate translation is in progress
                Toast.makeText(
                    this@MainActivity,
                    "Translating page to ${getLanguageName(targetLanguage)}",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Get the current URL
                val currentUrl = binding.webView.url ?: return@launch
                
                // Create a Google Translate URL
                // Format: https://translate.google.com/translate?sl=auto&tl=TARGET_LANG&u=URL
                val translateUrl = "https://translate.google.com/translate?sl=auto&tl=$targetLanguage&u=${java.net.URLEncoder.encode(currentUrl, "UTF-8")}"
                
                // Load the Google Translate URL
                binding.webView.loadUrl(translateUrl)
                
                // Show a toast to indicate translation is complete
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(
                        this@MainActivity,
                        "Page translated to ${getLanguageName(targetLanguage)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }, 2000) // Delay to allow translation to complete
            } catch (e: Exception) {
                // Show error message
                Toast.makeText(
                    this@MainActivity,
                    "Translation failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        
        // Get the selected text
        val webView = v as WebView
        val hitTestResult = webView.hitTestResult
        
        // Get selected text via JavaScript
        webView.evaluateJavascript(
            "(function() { return window.getSelection().toString(); })();",
            { value ->
                // Remove quotes from the returned value
                selectedText = value.replace("\"", "")
                
                if (selectedText.isNotEmpty()) {
                    // Add menu items to the context menu
                    menu?.add(0, 1, 0, getString(R.string.translation))
                }
            }
        )
    }
    
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                // Show translation dialog
                if (selectedText.isNotEmpty()) {
                    showTranslationDialog(selectedText)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
    
    private fun showTranslationDialog(text: String) {
        lifecycleScope.launch {
            // Detect the source language
            val sourceLanguage = viewModel.detectLanguage(text)
            
            // Get the target language from settings
            val settings = viewModel.getSettings()
            val targetLanguage = settings.targetLanguage
            
            // Translate the text
            val translatedText = viewModel.translateText(text, sourceLanguage, targetLanguage)
            
            // Show the translation dialog
            val dialog = TranslationDialogFragment.newInstance(
                text,
                translatedText,
                sourceLanguage,
                targetLanguage
            )
            dialog.show(supportFragmentManager, "translation_dialog")
        }
    }
    
    override fun onAddToVocabulary(
        originalWord: String,
        translatedWord: String,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        // Add the word to the vocabulary list
        val vocabularyItem = VocabularyItem(
            originalWord = originalWord,
            translatedWord = translatedWord,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        
        lifecycleScope.launch {
            viewModel.addVocabularyItem(vocabularyItem)
            // No toast message needed here
        }
    }
    
    private fun getLanguageName(languageCode: String): String {
        val languageCodes = resources.getStringArray(R.array.language_codes)
        val languageNames = resources.getStringArray(R.array.languages)
        
        val index = languageCodes.indexOf(languageCode)
        return if (index != -1) {
            languageNames[index]
        } else {
            languageCode
        }
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
