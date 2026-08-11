package com.indolearn.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize with Indonesian as default, can change on-the-fly
                val result = tts?.setLanguage(Locale("id", "ID"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speak(text: String, rate: Float = 1.0f, langCode: String = "ID") {
        if (isReady) {
            val locale = if (langCode == "TR") {
                Locale("tr", "TR")
            } else {
                Locale("id", "ID")
            }
            tts?.setLanguage(locale)
            tts?.setSpeechRate(rate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
