package com.nammasanthe.ledger.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

object MachineTranslationService {
    private val cache = ConcurrentHashMap<String, String>()

    suspend fun preloadEnglishKannadaModels() {
        preloadModelPair(TranslateLanguage.ENGLISH, TranslateLanguage.KANNADA)
        preloadModelPair(TranslateLanguage.KANNADA, TranslateLanguage.ENGLISH)
    }

    suspend fun translateForDisplay(text: String, targetLanguageCode: String): String {
        val input = text.trim()
        if (input.isBlank()) return text

        val targetLanguage = targetLanguageFromCode(targetLanguageCode)
        val sourceLanguage = sourceLanguageFromText(input)

        if (sourceLanguage == targetLanguage) return text

        val cacheKey = "$sourceLanguage->$targetLanguage|$input"
        cache[cacheKey]?.let { return it }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        val translator = Translation.getClient(options)
        return try {
            translator.downloadModelIfNeeded().await()
            val translated = translator.translate(input).await()
            cache[cacheKey] = translated
            translated
        } catch (_: Exception) {
            text
        } finally {
            translator.close()
        }
    }

    private fun targetLanguageFromCode(languageCode: String): String {
        return if (languageCode == "kn") TranslateLanguage.KANNADA else TranslateLanguage.ENGLISH
    }

    private suspend fun preloadModelPair(source: String, target: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()
        val translator = Translation.getClient(options)
        try {
            translator.downloadModelIfNeeded().await()
        } catch (_: Exception) {
            // Ignore preload failures; translation calls still retry lazily.
        } finally {
            translator.close()
        }
    }

    private fun sourceLanguageFromText(text: String): String {
        val hasKannada = text.any { it in '\u0C80'..'\u0CFF' }
        return if (hasKannada) TranslateLanguage.KANNADA else TranslateLanguage.ENGLISH
    }
}

@Composable
fun rememberTranslatedText(original: String, targetLanguageCode: String): State<String> {
    return produceState(initialValue = original, original, targetLanguageCode) {
        value = MachineTranslationService.translateForDisplay(original, targetLanguageCode)
    }
}
