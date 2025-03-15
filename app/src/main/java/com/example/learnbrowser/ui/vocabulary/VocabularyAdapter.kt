package com.example.learnbrowser.ui.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.databinding.ItemVocabularyBinding
import com.example.learnbrowser.ui.getLanguageName
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for the vocabulary items RecyclerView.
 */
class VocabularyAdapter(
    private val onDeleteClick: (VocabularyItem) -> Unit
) : ListAdapter<VocabularyItem, VocabularyAdapter.VocabularyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyViewHolder {
        val binding = ItemVocabularyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VocabularyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VocabularyViewHolder(
        private val binding: ItemVocabularyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VocabularyItem) {
            binding.apply {
                // Set text values
                originalWordTextView.text = item.originalWord
                translatedWordTextView.text = item.translatedWord
                
                // Set source language
                val sourceLanguageName = binding.root.context.getLanguageName(item.sourceLanguage)
                sourceLanguageTextView.text = sourceLanguageName
                
                // Format date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(item.dateAdded)
                dateAddedTextView.text = "Added: $formattedDate"
                
                // Set delete button click listener
                deleteButton.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VocabularyItem>() {
            override fun areItemsTheSame(oldItem: VocabularyItem, newItem: VocabularyItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VocabularyItem, newItem: VocabularyItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
