package com.nammasanthe.ledger.utils

import android.icu.text.Transliterator

object TransliterationUtils {
    private val latinToKannada: Transliterator? = try {
        Transliterator.getInstance("Latin-Kannada")
    } catch (e: Exception) {
        null
    }

    fun toKannada(input: String): String {
        if (input.isBlank()) return input
        return try {
            // Use `transliterate` which exists on android.icu.text.Transliterator
            latinToKannada?.transliterate(input) ?: input
        } catch (e: Exception) {
            // Fallback: return original if transliteration not available
            input
        }
    }
}
