package com.example.learnbrowser.ui.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnbrowser.data.model.VocabularyItem
import com.example.learnbrowser.databinding.ItemVocabularyBinding
import com.example.learnbrowser.databinding.ItemVocabularyLearnBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for the vocabulary items RecyclerView.
 */
class VocabularyAdapter(
    private val onDeleteClick: (VocabularyItem) -> Unit
) : ListAdapter<VocabularyItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // View types
    private val VIEW_TYPE_LIST = 0
    private val VIEW_TYPE_LEARN = 1
    
    // Current display mode
    private var isLearnMode = false
    
    fun setLearnMode(learnMode: Boolean) {
        if (isLearnMode != learnMode) {
            isLearnMode = learnMode
            notifyDataSetChanged()
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (isLearnMode) VIEW_TYPE_LEARN else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LIST -> {
                val binding = ItemVocabularyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ListViewHolder(binding)
            }
            VIEW_TYPE_LEARN -> {
                val binding = ItemVocabularyLearnBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LearnViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ListViewHolder -> holder.bind(item)
            is LearnViewHolder -> holder.bind(item)
        }
    }

    inner class ListViewHolder(
        private val binding: ItemVocabularyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VocabularyItem) {
            binding.apply {
                // Set text values
                originalWordTextView.text = item.originalWord
                translatedWordTextView.text = item.translatedWord
                
                // Set language direction
                val sourceCode = item.sourceLanguage.uppercase().take(2)
                val targetCode = item.targetLanguage.uppercase().take(2)
                languageDirectionTextView.text = "$sourceCode > $targetCode"
                
                // Store source language in hidden view for compatibility
                sourceLanguageTextView.text = item.sourceLanguage
                
                // Format date
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val formattedDate = dateFormat.format(item.dateAdded)
                dateAddedTextView.text = formattedDate
                
                // Set delete button click listener
                deleteButton.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }
    
    inner class LearnViewHolder(
        private val binding: ItemVocabularyLearnBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VocabularyItem) {
            binding.apply {
                // Set text values
                originalWordTextView.text = item.originalWord
                translatedWordTextView.text = item.translatedWord
                
                // Store source language in hidden view for compatibility
                sourceLanguageTextView.text = item.sourceLanguage
                
                // Initially show the cover
                wordCoverView.visibility = View.VISIBLE
                
                // Set click listener on the cover to reveal the translated word
                wordCoverView.setOnClickListener {
                    wordCoverView.visibility = View.GONE
                }
                
                // Set click listener on the translated word to hide it again
                translatedWordTextView.setOnClickListener {
                    wordCoverView.visibility = View.VISIBLE
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
