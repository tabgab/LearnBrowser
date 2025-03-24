package com.example.learnbrowser.ui.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.learnbrowser.R
import com.example.learnbrowser.ui.getLanguageName

class LanguageFilterAdapter(
    private val languages: List<String>,
    private val selectedLanguages: MutableSet<String>,
    private val onLanguageSelectionChanged: (String, Boolean) -> Unit
) : RecyclerView.Adapter<LanguageFilterAdapter.ViewHolder>() {

    class ViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val checkBox = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language_filter, parent, false) as CheckBox
        return ViewHolder(checkBox)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val languageCode = languages[position]
        val languageName = holder.checkBox.context.getLanguageName(languageCode)
        
        holder.checkBox.text = languageName
        holder.checkBox.isChecked = selectedLanguages.contains(languageCode)
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedLanguages.add(languageCode)
            } else {
                selectedLanguages.remove(languageCode)
            }
            onLanguageSelectionChanged(languageCode, isChecked)
        }
    }

    override fun getItemCount() = languages.size
}
