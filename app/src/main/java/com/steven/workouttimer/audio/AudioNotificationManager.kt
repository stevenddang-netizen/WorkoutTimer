package com.steven.workouttimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.sin

class AudioNotificationManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var toneGenerator: ToneGenerator? = null

    companion object {
        private const val SAMPLE_RATE = 44100
    }

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
                AudioManager.STREAM_NOTIFICATION,
                100
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Plays a countdown beep with rising pitch based on seconds remaining.
     * The last beep (1 second) is longer (2 seconds duration).
     */
    fun playCountdownBeep(secondsRemaining: Int, maxCountdownSeconds: Int) {
        Thread {
            try {
                // Calculate pitch - rises as we get closer to 0
                // Start at 600Hz, end at 1200Hz for the final beep
                val pitchProgress = 1.0 - (secondsRemaining.toDouble() / maxCountdownSeconds)
                val frequency = 600.0 + (pitchProgress * 600.0) // 600Hz to 1200Hz

                // Duration: 200ms for normal beeps, 1000ms for the last beep
                val durationMs = if (secondsRemaining == 1) 1000 else 200

                // Volume: louder as we get closer (0.7 to 1.0)
                val volume = 0.7 + (pitchProgress * 0.3)

                playTone(frequency, durationMs, volume)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Double) {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)

        // Generate sine wave
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE
            var sample = sin(angle)

            // Apply fade in/out to avoid clicks (10ms fade)
            val fadeSamples = (SAMPLE_RATE * 0.01).toInt()
            if (i < fadeSamples) {
                sample *= i.toDouble() / fadeSamples
            } else if (i > numSamples - fadeSamples) {
                sample *= (numSamples - i).toDouble() / fadeSamples
            }

            samples[i] = (sample * volume * Short.MAX_VALUE).toInt().toShort()
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(maxOf(bufferSize, samples.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()

        // Wait for playback to complete
        Thread.sleep(durationMs.toLong() + 50)
        audioTrack.stop()
        audioTrack.release()
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
