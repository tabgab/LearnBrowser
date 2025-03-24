package com.example.learnbrowser.ui.vocabulary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnbrowser.R
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.databinding.ActivityVocabularyBinding
import com.example.learnbrowser.ui.base.BaseActivity
import com.example.learnbrowser.ui.getLanguageName
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VocabularyActivity : BaseActivity() {

    private lateinit var binding: ActivityVocabularyBinding
    private val viewModel: VocabularyViewModel by viewModels()
    
    private lateinit var adapter: VocabularyAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVocabularyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupLanguageFilter()
        setupButtons()
        setupObservers()
    }
    
    private fun setupToolbar() {
        // Set up back button click listener
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = VocabularyAdapter { item ->
            // Handle delete button click
            showDeleteConfirmationDialog(item)
        }
        
        binding.vocabularyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.vocabularyRecyclerView.adapter = adapter
    }
    
    private fun setupLanguageFilter() {
        // Set up the language filter button with localized text
        binding.languageFilterButton.text = getString(R.string.filter_by_language)
        binding.languageFilterButton.setOnClickListener {
            showLanguageFilterDialog()
        }
    }
    
    private fun showLanguageFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_language_filter, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val allLanguagesCheckbox = dialogView.findViewById<CheckBox>(R.id.allLanguagesCheckbox)
        val languagesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.languagesRecyclerView)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val applyButton = dialogView.findViewById<Button>(R.id.applyButton)
        
        // Set up RecyclerView
        languagesRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Get current selected languages
        val selectedLanguages = mutableSetOf<String>()
        
        viewModel.sourceLanguages.observe(this) { languages ->
            if (languages.isEmpty()) return@observe
            
            // Create adapter with languages
            val adapter = LanguageFilterAdapter(
                languages,
                selectedLanguages
            ) { language, selected ->
                // Toggle selection
                if (selected) {
                    selectedLanguages.add(language)
                    // If any language is selected, uncheck "All Languages"
                    allLanguagesCheckbox.isChecked = false
                } else {
                    selectedLanguages.remove(language)
                    // If no languages are selected, check "All Languages"
                    if (selectedLanguages.isEmpty()) {
                        allLanguagesCheckbox.isChecked = true
                    }
                }
            }
            
            languagesRecyclerView.adapter = adapter
        }
        
        // Set up "All Languages" checkbox
        allLanguagesCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Clear all selections
                selectedLanguages.clear()
                languagesRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
        
        // Set up buttons
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        applyButton.setOnClickListener {
            // Apply filter
            if (allLanguagesCheckbox.isChecked) {
                viewModel.setSelectedLanguages(emptySet())
                binding.languageFilterButton.text = getString(R.string.filter_by_language)
            } else {
                viewModel.setSelectedLanguages(selectedLanguages)
                // Update button text to show number of selected languages
                binding.languageFilterButton.text = getString(R.string.filter_by_language) + " (${selectedLanguages.size})"
            }
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun setupButtons() {
        // Mode toggle button
        var isLearnMode = false
        binding.modeToggleButton.setOnClickListener {
            isLearnMode = !isLearnMode
            
            // Update button text
            binding.modeToggleButton.text = getString(
                if (isLearnMode) R.string.learn_mode else R.string.list_mode
            )
            
            // Update adapter mode
            adapter.setLearnMode(isLearnMode)
            
            // Show/hide table header based on mode
            binding.tableHeaderLayout.visibility = if (isLearnMode) View.GONE else View.VISIBLE
        }
        
        // Clear all button
        binding.clearAllButton.setOnClickListener {
            showClearAllConfirmationDialog()
        }
        
        // Export button
        binding.exportButton.setOnClickListener {
            viewModel.exportVocabularyItems(this)
        }
    }
    
    private fun setupObservers() {
        // Observe vocabulary items
        viewModel.vocabularyItems.observe(this) { items ->
            adapter.submitList(items)
            
            // Show empty state if needed
            if (items.isEmpty()) {
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.vocabularyRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateTextView.visibility = View.GONE
                binding.vocabularyRecyclerView.visibility = View.VISIBLE
            }
        }
        
        // Observe errors
        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe export result
        viewModel.exportResult.observe(this) { (success, file) ->
            if (success && file != null) {
                // Show success message
                Toast.makeText(
                    this,
                    getString(R.string.export_success),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Share the file
                shareExportedFile(file)
            } else {
                // Show error message
                Toast.makeText(
                    this,
                    getString(R.string.export_failure),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showDeleteConfirmationDialog(item: VocabularyItem) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage("Delete \"${item.originalWord}\"?")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteVocabularyItem(item)
                
                // Show undo snackbar
                Snackbar.make(
                    binding.root,
                    getString(R.string.item_deleted),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.undo)) {
                        // Undo delete
                        viewModel.addVocabularyItem(item)
                    }
                    .show()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun showClearAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_all))
            .setMessage(getString(R.string.clear_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.clearAllVocabularyItems()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun shareExportedFile(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(intent, "Share Vocabulary List"))
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
