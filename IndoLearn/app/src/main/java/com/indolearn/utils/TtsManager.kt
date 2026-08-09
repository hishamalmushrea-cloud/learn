package com.indolearn.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("id", "ID"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speak(text: String, rate: Float = 1.0f) {
        if (isReady) {
            tts?.setSpeechRate(rate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun isIndonesianSupported(): Boolean = isReady

    fun shutdown() {
        tts?.shutdown()
    }
}