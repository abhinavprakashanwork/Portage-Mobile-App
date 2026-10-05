package com.example.ui.components

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Apple Intelligence Perimeter Glow Layer Configuration:
 *
 * Layer 1: t: 3px, blur: 15px, opacity: 1, breathe: false
 * Layer 2: t: 8px, blur: 12px, opacity: 1, breathe: false
 * Layer 3: t: 9px, blur: 28px, opacity: 1, breathe: true, delay: 0s
 * Layer 4: t: 18px, blur: 32px, opacity: 0.9, breathe: true, delay: 0.2s
 */
data class GlowLayer(
  val t: Float,       // thickness in dp
  val b: Float,       // blur in dp
  val o: Float,       // opacity
  val breathe: Boolean,
  val delayMs: Int
)

val GLOW_LAYERS: List<GlowLayer> = listOf(
  GlowLayer(t = 3f, b = 15f, o = 1f, breathe = false, delayMs = 0),
  GlowLayer(t = 8f, b = 12f, o = 1f, breathe = false, delayMs = 0),
  GlowLayer(t = 9f, b = 28f, o = 1f, breathe = true, delayMs = 0),
  GlowLayer(t = 18f, b = 32f, o = 0.9f, breathe = true, delayMs = 200)
)

/**
 * Exact Apple Intelligence conic-gradient palette:
 * conic-gradient(from var(--ig-angle), #bc82f3, #f5b9ea, #8d9fff, #ff6778, #ffba71, #c686ff, #bc82f3);
 */
private val APPLE_INTELLIGENCE_COLORS = intArrayOf(
  0xFFBC82F3.toInt(), // Lilac
  0xFFF5B9EA.toInt(), // Rose / Soft Pink
  0xFF8D9FFF.toInt(), // Periwinkle Blue
  0xFFFF6778.toInt(), // Coral Red
  0xFFFFBA71.toInt(), // Peach / Amber
  0xFFC686FF.toInt(), // Bright Violet
  0xFFBC82F3.toInt()  // Wrap back to Lilac
)

/**
 * Exact 1IAD Apple Intelligence Audio Feedback System:
 * - On summon (on = true): Plays the official Apple Intelligence summon sound (R.raw.intelligence_summon)
 * - On dismiss (on = false): Synthesizes the exact dual-sine exponential down-ramps:
 *   Tone 1: 784 Hz -> 392 Hz (exponential ramp over 0.35s)
 *   Tone 2: 1046 Hz -> 523 Hz (exponential ramp over 0.35s, 0.05s delay)
 */
object IntelligenceAudioFeedback {
  @Volatile
  private var appContext: Context? = null

  fun init(context: Context) {
    appContext = context.applicationContext
  }

  fun playChime(on: Boolean) {
    if (on) {
      val ctx = appContext
      if (ctx != null) {
        try {
          val mp = MediaPlayer.create(ctx, R.raw.intelligence_summon)
          if (mp != null) {
            mp.setOnCompletionListener {
              try {
                it.reset()
                it.release()
              } catch (_: Exception) {}
            }
            mp.start()
            return
          }
        } catch (_: Exception) {}
      }
    }

    // Dismiss audio (or fallback for summon):
    // Dual descending sine ramps matching 1iad.com/r/intelligence-glow
    Thread {
      try {
        val sampleRate = 44100
        val durationSec = 0.55
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until numSamples) {
          val t = i.toDouble() / sampleRate

          // Tone 1: 784 -> 392
          val f1 = if (t <= 0.35) 784.0 * Math.pow(0.5, t / 0.35) else 392.0
          phase1 += 2.0 * Math.PI * f1 / sampleRate
          val g1 = when {
            t < 0.05 -> 0.0001 * Math.pow(0.06 / 0.0001, t / 0.05)
            t < 0.50 -> 0.06 * Math.pow(0.0001 / 0.06, (t - 0.05) / 0.45)
            else -> 0.0
          }

          // Tone 2: 1046 -> 523, delay = 0.05s
          val t2 = t - 0.05
          val g2 = if (t2 >= 0) {
            val f2 = if (t2 <= 0.35) 1046.0 * Math.pow(0.5, t2 / 0.35) else 523.0
            phase2 += 2.0 * Math.PI * f2 / sampleRate
            when {
              t2 < 0.05 -> 0.0001 * Math.pow(0.06 / 0.0001, t2 / 0.05)
              t2 < 0.50 -> 0.06 * Math.pow(0.0001 / 0.06, (t2 - 0.05) / 0.45)
              else -> 0.0
            }
          } else {
            0.0
          }

          val mixed = g1 * Math.sin(phase1) + g2 * Math.sin(phase2)
          val scaled = (mixed * 5.0 * Short.MAX_VALUE).toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
          buffer[i] = scaled.toShort()
        }

        val track = AudioTrack(
          AudioManager.STREAM_MUSIC,
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          buffer.size * 2,
          AudioTrack.MODE_STATIC
        )
        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep((durationSec * 1000).toLong() + 50)
        track.release()
      } catch (_: Exception) {
        // Fallback silently
      }
    }.start()
  }
}

/**
 * Inner Apple Intelligence Perimeter Glow Overlay:
 * Renders the 4 glow layers strictly INSIDE the container along its inner perimeter.
 * Does not bleed or cast outside the box.
 */
@Composable
fun IntelligenceGlowOverlay(
  active: Boolean = true,
  cornerRadius: Dp = 16.dp,
  scale: Float = 1.0f,
  modifier: Modifier = Modifier
) {
  // Performance safeguard: do not run continuous blurs when inactive
  if (!active) return

  val infiniteTransition = rememberInfiniteTransition(label = "ig_transition")

  // @keyframes ig-spin: rotating --ig-angle from 0deg to 360deg over 5s linear infinite
  val angle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ig_angle"
  )

  // @keyframes ig-breathe: oscillating opacity between 0.85 and 0.35 over 3.2s ease-in-out infinite
  val breathe0s by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ig_breathe_0s"
  )

  // Breathing with 0.2s delay for Layer 4
  val breathe02s by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, delayMillis = 200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ig_breathe_02s"
  )

  val paint = remember {
    Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(cornerRadius))
      .drawBehind {
        val canvas = drawContext.canvas.nativeCanvas
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rPx = cornerRadius.toPx()

        // Conic gradient rotating around center
        val sweep = SweepGradient(cx, cy, APPLE_INTELLIGENCE_COLORS, null)
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle, cx, cy)
        sweep.setLocalMatrix(matrix)
        paint.shader = sweep

        // Stack 4 inward perimeter layers
        for (layer in GLOW_LAYERS) {
          val breatheFactor = if (layer.breathe) {
            if (layer.delayMs > 0) breathe02s else breathe0s
          } else {
            1.0f
          }
          val currentOpacity = layer.o * breatheFactor
          val alpha = (currentOpacity * 255f).toInt().coerceIn(0, 255)
          val tPx = (layer.t * scale).dp.toPx().coerceAtLeast(1.5f)
          val bPx = (layer.b * scale * breatheFactor).dp.toPx().coerceAtLeast(1f)

          paint.alpha = alpha
          paint.strokeWidth = tPx
          paint.maskFilter = BlurMaskFilter(bPx, BlurMaskFilter.Blur.NORMAL)

          val rect = RectF(
            tPx / 2f,
            tPx / 2f,
            w - tPx / 2f,
            h - tPx / 2f
          )
          val adjustedR = (rPx - tPx / 2f).coerceAtLeast(0f)
          canvas.drawRoundRect(rect, adjustedR, adjustedR, paint)
        }

        // Crisp inner edge highlight (subtle)
        paint.maskFilter = null
        paint.alpha = 150
        paint.strokeWidth = 1.5.dp.toPx()
        val innerRect = RectF(
          0.75.dp.toPx(),
          0.75.dp.toPx(),
          w - 0.75.dp.toPx(),
          h - 0.75.dp.toPx()
        )
        canvas.drawRoundRect(innerRect, rPx, rPx, paint)
      }
  )
}
