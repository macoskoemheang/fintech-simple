package com.example.m3showcase.core.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the translations without an emulator:
 * every English key exists in each language, and placeholders (%1$s, %2$d) match,
 * because a wrong placeholder crashes String.format at runtime.
 */
class TranslationsTest {

    private val res = File("src/main/res")
    private val languages = listOf("km", "zh")

    /** name → text of every <string> and <plurals> entry (plural items joined). */
    private fun load(folder: String): Map<String, String> {
        val xml = File(res, "$folder/strings.xml").readText()
        val strings = Regex("""<string name="([^"]+)"(?: [^>]*)?>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml).associate { it.groupValues[1] to it.groupValues[2] }
        val plurals = Regex("""<plurals name="([^"]+)">(.*?)</plurals>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml).associate { it.groupValues[1] to it.groupValues[2] }
        return strings + plurals
    }

    private fun placeholders(text: String) = Regex("""%\d+\$[sd]""").findAll(text).map { it.value }.toSortedSet()

    private val english = load("values").filterKeys { it != "app_name" }

    @Test
    fun `every language has every key`() {
        languages.forEach { lang ->
            val translated = load("values-$lang")
            val missing = english.keys - translated.keys
            assertTrue("values-$lang is missing $missing", missing.isEmpty())
            val stale = translated.keys - english.keys
            assertTrue("values-$lang has keys English no longer has: $stale", stale.isEmpty())
        }
    }

    @Test
    fun `placeholders match English`() {
        languages.forEach { lang ->
            val translated = load("values-$lang")
            english.forEach { (key, text) ->
                assertEquals("values-$lang/$key", placeholders(text), placeholders(translated.getValue(key)))
            }
        }
    }

    @Test
    fun `no translation is left empty`() {
        languages.forEach { lang ->
            load("values-$lang").forEach { (key, text) -> assertTrue("values-$lang/$key is empty", text.isNotBlank()) }
        }
    }
}
