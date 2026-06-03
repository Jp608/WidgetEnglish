package com.jp.widgetenglish.features.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return

        WidgetTtsPlayer.speak(context.applicationContext, text)
    }

    private object WidgetTtsPlayer {
        private const val DUPLICATE_SOUND_WINDOW_MS = 1_500L
        private const val UTTERANCE_PREFIX = "WIDGET_TTS"

        private val lock = Any()
        private var tts: TextToSpeech? = null
        private var initializing = false
        private var pendingText: String? = null
        private var lastText: String? = null
        private var lastRequestAt = 0L
        private var isSpeaking = false
        private var utteranceSequence = 0
        private var activeUtteranceId: String? = null

        fun speak(context: Context, text: String) {
            val appContext = context.applicationContext
            var engineToUse: TextToSpeech? = null
            var shouldInitialize = false
            var shouldIgnore = false

            synchronized(lock) {
                val now = SystemClock.elapsedRealtime()
                val isRepeatedText = text == lastText
                val isFastRepeat = now - lastRequestAt <= DUPLICATE_SOUND_WINDOW_MS

                if (isRepeatedText && (isSpeaking || isFastRepeat || initializing)) {
                    shouldIgnore = true
                    return@synchronized
                }

                lastText = text
                lastRequestAt = now

                val currentTts = tts
                if (currentTts != null) {
                    engineToUse = currentTts
                } else {
                    pendingText = text

                    if (!initializing) {
                        initializing = true
                        shouldInitialize = true
                    }
                }
            }

            if (shouldIgnore) return

            engineToUse?.let { engine ->
                speakNow(engine, text)
                return
            }

            if (shouldInitialize) {
                initialize(appContext)
            }
        }

        private fun initialize(context: Context) {
            var createdTts: TextToSpeech? = null

            createdTts = TextToSpeech(context.applicationContext) { status ->
                val engine = createdTts

                if (status == TextToSpeech.SUCCESS && engine != null) {
                    engine.language = Locale.US
                    engine.setOnUtteranceProgressListener(createProgressListener())

                    val textToSpeak = synchronized(lock) {
                        tts = engine
                        initializing = false

                        pendingText.also {
                            pendingText = null
                        }
                    }

                    textToSpeak?.let { speakNow(engine, it) }
                } else {
                    synchronized(lock) {
                        initializing = false
                        pendingText = null
                        isSpeaking = false
                        activeUtteranceId = null
                    }
                }
            }
        }

        private fun speakNow(engine: TextToSpeech, text: String) {
            val utteranceId = synchronized(lock) {
                utteranceSequence += 1
                "$UTTERANCE_PREFIX-$utteranceSequence".also {
                    activeUtteranceId = it
                    isSpeaking = true
                }
            }

            engine.stop()

            val result = engine.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )

            if (result == TextToSpeech.ERROR) {
                markFinished(utteranceId)
            }
        }

        private fun createProgressListener(): UtteranceProgressListener {
            return object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == null) return

                    synchronized(lock) {
                        if (utteranceId == activeUtteranceId) {
                            isSpeaking = true
                        }
                    }
                }

                override fun onDone(utteranceId: String?) {
                    markFinished(utteranceId)
                }

                override fun onError(utteranceId: String?) {
                    markFinished(utteranceId)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    markFinished(utteranceId)
                }
            }
        }

        private fun markFinished(utteranceId: String?) {
            synchronized(lock) {
                if (utteranceId == activeUtteranceId) {
                    isSpeaking = false
                    activeUtteranceId = null
                }
            }
        }
    }
}
