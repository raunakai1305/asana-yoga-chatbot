package com.asana.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.75f)
                isTtsReady = true
            }
        }
    }

    fun startListening(onResult: (String) -> Unit, onError: () -> Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) onResult(text) else onError()
            }
            override fun onError(error: Int) = onError()
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "en-US"))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        val clean = text
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")  // **bold** → bold
            .replace(Regex("\\*(.+?)\\*"), "$1")         // *italic* → italic
            .replace(Regex("#+\\s*"), "")                // ### headings
            .replace(Regex("(?m)^[-•*]\\s+"), "")       // bullet points
            .replace(Regex("(?m)^\\d+\\.\\s+"), "")     // numbered lists
            .replace(Regex("`+"), "")                    // backticks
            .replace(Regex("_+"), "")                    // underscores
            .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1") // [links](url) → link text
            .replace(Regex("\\s{2,}"), " ")              // collapse whitespace
            .trim()
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
    }
}
