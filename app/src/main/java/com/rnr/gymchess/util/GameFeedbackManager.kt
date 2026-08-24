package com.rnr.gymchess.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

class GameFeedbackManager(context: Context) : GameFeedback {

    private val appContext = context.applicationContext

    override fun onPlayerTimedOut() {
        playTimeoutSound()
        vibrateTimeout()
    }

    private fun playTimeoutSound() {
        var tone: ToneGenerator? = null
        try {
            tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
        } catch (_: Exception) {
            // Ignore if audio is unavailable.
        } finally {
            tone?.release()
        }
    }

    private fun vibrateTimeout() {
        val vibrator = ContextCompat.getSystemService(appContext, Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    TIMEOUT_VIBRATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(TIMEOUT_VIBRATION_MS)
        }
    }

    companion object {
        private const val TIMEOUT_VIBRATION_MS = 400L
    }
}
