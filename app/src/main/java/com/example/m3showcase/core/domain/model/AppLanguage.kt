package com.example.m3showcase.core.domain.model

/**
 * Languages the app ships. [tag] is a BCP 47 tag; null follows the device language.
 * Adding one: add the enum entry, a res/values-<lang>/strings.xml and a line in res/xml/locales_config.xml.
 */
enum class AppLanguage(val tag: String?) {
    System(null),
    English("en"),
    Khmer("km"),
    Chinese("zh-CN"),
    ;

    companion object {
        /** Maps a stored tag back to a language. Unknown or empty means "follow the device". */
        fun fromTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return System
            val language = tag.substringBefore('-').lowercase()
            return entries.firstOrNull { it.tag?.substringBefore('-') == language } ?: System
        }
    }
}
