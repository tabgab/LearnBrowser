package com.example.learnbrowser.data.translation

/**
 * Enum representing the different translation services supported by the application.
 */
enum class TranslationServiceType(val displayName: String, val requiresApiKey: Boolean) {
    GOOGLE_TRANSLATE("Google Translate", true),
    LIBRE_TRANSLATE("LibreTranslate", true),
    DEEPL("DeepL", true),
    LINGVA_TRANSLATE("Lingva Translate", false),
    ARGOS_TRANSLATE("Argos Translate (Offline)", false),
    MICROSOFT_TRANSLATOR("Microsoft Translator", true),
    YANDEX_TRANSLATE("Yandex.Translate", true);

    companion object {
        /**
         * Get the default translation service type.
         */
        fun getDefault(): TranslationServiceType = LINGVA_TRANSLATE

        /**
         * Get a translation service type by its name.
         */
        fun fromName(name: String): TranslationServiceType {
            return values().find { it.name == name } ?: getDefault()
        }
    }
}
