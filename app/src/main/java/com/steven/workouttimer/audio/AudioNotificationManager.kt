package com.steven.workouttimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AudioNotificationManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var toneGenerator: ToneGenerator? = null

    init {
        initializeTTS()
        initializeToneGenerator()
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.US)
                    ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED

                    tts.setSpeechRate(1.2f)
                    tts.setPitch(1.0f)
                }
            }
        }
    }

    private fun initializeToneGenerator() {
        try {
            toneGenerator = ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                100
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playDoubleBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speakNumber(number: Int) {
        if (ttsReady) {
            textToSpeech?.speak(
                number.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "countdown_$number"
            )
        }
    }

    fun speakText(text: String) {
        if (ttsReady) {
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "speech_${System.currentTimeMillis()}"
            )
        }
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        toneGenerator?.release()
        toneGenerator = null
    }
}
