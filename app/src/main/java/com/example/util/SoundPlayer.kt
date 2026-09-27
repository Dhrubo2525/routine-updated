package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundPlayer {
    fun playSound(context: Context, soundType: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                when (soundType) {
                    "Gentle Chime" -> {
                        toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
                        delay(180)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
                    }
                    "Bell" -> {
                        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 350)
                    }
                    "Double Beep" -> {
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                        delay(200)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                    }
                    "Soft Pulse" -> {
                        toneGen.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 200)
                    }
                    else -> {
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                    }
                }
                delay(400)
                toneGen.release()
            } catch (e: Exception) {
                // Ignore audio device errors gracefully
            }
        }

        // Haptic feedback
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(120)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
}
