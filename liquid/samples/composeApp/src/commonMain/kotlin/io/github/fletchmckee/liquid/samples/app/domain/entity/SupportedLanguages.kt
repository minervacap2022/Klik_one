package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Represents a language supported by the Klik platform.
 * Used for language selection in ProfileScreen and sent to LLM-powered APIs.
 */
data class SupportedLanguage(
    val code: String,        // ISO 639-1 code (e.g., "en", "zh", "es")
    val englishName: String, // English name (e.g., "Chinese")
    val nativeName: String   // Native name (e.g., "中文")
) {
    /** Display label: "English name - Native name" (or just native name if same) */
    val displayName: String
        get() = if (englishName == nativeName) nativeName else "$englishName - $nativeName"
}

/**
 * All languages supported by the Klik platform.
 * These are passed as the "language" parameter to LLM-powered backend APIs
 * (insights, encourage, worklife, chat).
 */
val SUPPORTED_LANGUAGES = listOf(
    SupportedLanguage("en", "English", "English"),
    SupportedLanguage("zh", "Chinese", "中文"),
    SupportedLanguage("es", "Spanish", "Español"),
    SupportedLanguage("fr", "French", "Français"),
    SupportedLanguage("de", "German", "Deutsch"),
    SupportedLanguage("ja", "Japanese", "日本語"),
    SupportedLanguage("ko", "Korean", "한국어"),
    SupportedLanguage("pt", "Portuguese", "Português"),
    SupportedLanguage("ru", "Russian", "Русский"),
    SupportedLanguage("ar", "Arabic", "العربية"),
    SupportedLanguage("hi", "Hindi", "हिन्दी"),
    SupportedLanguage("it", "Italian", "Italiano"),
    SupportedLanguage("nl", "Dutch", "Nederlands"),
    SupportedLanguage("th", "Thai", "ไทย"),
    SupportedLanguage("vi", "Vietnamese", "Tiếng Việt"),
    SupportedLanguage("id", "Indonesian", "Bahasa Indonesia"),
    SupportedLanguage("ms", "Malay", "Bahasa Melayu"),
    SupportedLanguage("tr", "Turkish", "Türkçe"),
    SupportedLanguage("pl", "Polish", "Polski"),
    SupportedLanguage("sv", "Swedish", "Svenska")
)

/**
 * Find a SupportedLanguage by its ISO code.
 * Returns English as default if the code is not found.
 */
fun findLanguageByCode(code: String): SupportedLanguage =
    SUPPORTED_LANGUAGES.find { it.code == code } ?: SUPPORTED_LANGUAGES.first()
