package com.example.learnbrowser.ui.translation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.learnbrowser.databinding.DialogTranslationBinding
import com.example.learnbrowser.ui.getLanguageName

/**
 * Dialog fragment for displaying translations.
 */
class TranslationDialogFragment : DialogFragment() {

    private var _binding: DialogTranslationBinding? = null
    private val binding get() = _binding!!
    
    private var listener: TranslationDialogListener? = null
    
    // Translation data
    private lateinit var originalWord: String
    private lateinit var translatedWord: String
    private lateinit var sourceLanguage: String
    private lateinit var targetLanguage: String
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TranslationDialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement TranslationDialogListener")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get arguments
        arguments?.let {
            originalWord = it.getString(ARG_ORIGINAL_WORD, "")
            translatedWord = it.getString(ARG_TRANSLATED_WORD, "")
            sourceLanguage = it.getString(ARG_SOURCE_LANGUAGE, "")
            targetLanguage = it.getString(ARG_TARGET_LANGUAGE, "")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTranslationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up the dialog content
        binding.originalWordTextView.text = originalWord
        binding.translatedWordTextView.text = translatedWord
        
        // Set up the buttons
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        binding.addToVocabularyButton.setOnClickListener {
            listener?.onAddToVocabulary(
                originalWord,
                translatedWord,
                sourceLanguage,
                targetLanguage
            )
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Interface for communication with the host activity.
     */
    interface TranslationDialogListener {
        fun onAddToVocabulary(
            originalWord: String,
            translatedWord: String,
            sourceLanguage: String,
            targetLanguage: String
        )
    }
    
    companion object {
        private const val ARG_ORIGINAL_WORD = "original_word"
        private const val ARG_TRANSLATED_WORD = "translated_word"
        private const val ARG_SOURCE_LANGUAGE = "source_language"
        private const val ARG_TARGET_LANGUAGE = "target_language"
        
        /**
         * Create a new instance of the dialog with the provided translation data.
         */
        fun newInstance(
            originalWord: String,
            translatedWord: String,
            sourceLanguage: String,
            targetLanguage: String
        ): TranslationDialogFragment {
            return TranslationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORIGINAL_WORD, originalWord)
                    putString(ARG_TRANSLATED_WORD, translatedWord)
                    putString(ARG_SOURCE_LANGUAGE, sourceLanguage)
                    putString(ARG_TARGET_LANGUAGE, targetLanguage)
                }
            }
        }
    }
}
