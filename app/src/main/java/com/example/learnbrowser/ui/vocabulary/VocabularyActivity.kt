package com.example.learnbrowser.ui.vocabulary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
        // Set up the language filter dropdown
        val allLanguagesItem = getString(R.string.all_languages)
        
        viewModel.sourceLanguages.observe(this) { languages ->
            val items = mutableListOf(allLanguagesItem)
            
            // Add language names
            languages.forEach { languageCode ->
                items.add(getLanguageName(languageCode))
            }
            
            val adapter = ArrayAdapter(
                this,
                R.layout.item_dropdown,
                items
            )
            
            binding.languageFilterAutoComplete.setAdapter(adapter)
            
            // Set default selection
            binding.languageFilterAutoComplete.setText(allLanguagesItem, false)
            
            // Handle selection
            binding.languageFilterAutoComplete.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    // "All Languages" selected
                    viewModel.setLanguageFilter(null)
                } else {
                    // Specific language selected
                    val languageCode = languages[position - 1]
                    viewModel.setLanguageFilter(languageCode)
                }
            }
        }
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
