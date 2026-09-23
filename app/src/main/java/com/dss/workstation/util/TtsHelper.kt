package com.dss.workstation.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Locale.US
                val fallbackResult = tts?.setLanguage(Locale.US)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Default and US TTS voice data missing or not supported.")
                    _lastError.value = "TTS voice data unavailable on this device."
                    _isReady.value = false
                    return
                }
            }
            // Speech rate tuned for clear cognitive accessibility (0.88f)
            tts?.setSpeechRate(0.88f)
            tts?.setPitch(1.0f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _lastError.value = "Speech playback error."
                }
            })
            _isReady.value = true
            Log.d(TAG, "TTS initialized successfully.")
        } else {
            Log.e(TAG, "TTS initialization failed with code: $status")
            _lastError.value = "Text-to-speech could not be initialized."
            _isReady.value = false
        }
    }

    fun speak(text: String, utteranceId: String = "dss_utterance") {
        if (!_isReady.value || tts == null) {
            _lastError.value = "Voice speech is currently unavailable."
            return
        }
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }

    companion object {
        private const val TAG = "TtsHelper"
    }
}
