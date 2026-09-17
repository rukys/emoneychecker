package com.emoneychecker.ui.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object HapticEngine {

    fun onCardDetected(context: Context) {
        runCatching {
            val vibrator = context.getSystemService(Vibrator::class.java) ?: return@runCatching
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        }
    }

    fun onReadSuccess(context: Context) {
        runCatching {
            val vibrator = context.getSystemService(Vibrator::class.java) ?: return@runCatching
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40)
            }
        }
    }

    fun onError(context: Context) {
        runCatching {
            val vibrator = context.getSystemService(Vibrator::class.java) ?: return@runCatching
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 60, 35)
                val amplitudes = intArrayOf(0, 180, 0, 180)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 60, 35), -1)
            }
        }
    }
}
