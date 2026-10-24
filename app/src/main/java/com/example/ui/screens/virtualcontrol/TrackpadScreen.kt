package com.example.ui.screens.virtualcontrol

import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.PrivacyModeManager
import com.example.ui.components.IntelligenceAudioFeedback
import com.example.ui.components.PrivacyModeCanvasBadge
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlinx.coroutines.delay
import kotlin.math.sqrt

// Exact Apple Intelligence Conic Gradient Spectrum
private val APPLE_INTELLIGENCE_COLORS = intArrayOf(
  0xFFBC82F3.toInt(), // Lilac
  0xFFF5B9EA.toInt(), // Soft Pink
  0xFF8D9FFF.toInt(), // Periwinkle Blue
  0xFFFF6778.toInt(), // Coral Red
  0xFFFFBA71.toInt(), // Warm Peach / Amber
  0xFFC686FF.toInt(), // Bright Violet
  0xFFBC82F3.toInt()  // Wrap back to Lilac
)

private data class OuterGlowLayer(
  val t: Float,       // thickness in dp
  val b: Float,       // blur in dp
  val o: Float,       // opacity
  val breathe: Boolean,
  val delayMs: Int
)

// Subtle, refined ambient glow layers for the trackpad
private val OUTER_GLOW_LAYERS = listOf(
  OuterGlowLayer(t = 3f, b = 10f, o = 0.35f, breathe = false, delayMs = 0),
  OuterGlowLayer(t = 7f, b = 18f, o = 0.25f, breathe = true, delayMs = 0),
  OuterGlowLayer(t = 12f, b = 26f, o = 0.16f, breathe = true, delayMs = 200)
)

enum class TrackpadInputMode {
  PURE_TEXT,
  SHORTCUT_KEY
}

@Composable
fun TrackpadScreen(
  onBackClick: () -> Unit,
  onMouseMove: (Float, Float) -> Unit,
  onMouseClick: (String) -> Unit,
  onSendText: (String) -> Unit,
  onSendShortcut: (String) -> Unit,
  onPlayAction: () -> Unit = {}
) {
  val haptics = LocalHapticFeedback.current
  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()

  var isPointerActive by remember { mutableStateOf(false) }
  var inputMode by remember { mutableStateOf(TrackpadInputMode.PURE_TEXT) }
  var inputText by remember { mutableStateOf("") }
  var showModeMenu by remember { mutableStateOf(false) }
  var lastCanvasKeyBadge by remember { mutableStateOf<String?>(null) }

  val togglePointerActive: () -> Unit = {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    isPointerActive = !isPointerActive
    IntelligenceAudioFeedback.playChime(isPointerActive)
    onPlayAction()
  }

  val triggerKeyBadge: (String) -> Unit = { key ->
    lastCanvasKeyBadge = key
  }

  LaunchedEffect(lastCanvasKeyBadge) {
    if (lastCanvasKeyBadge != null) {
      delay(1600)
      lastCanvasKeyBadge = null
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("trackpad_screen")
  ) {
    if (isLandscape) {
      // Landscape Layout (matching iPhone 17 - 163.png)
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        // Left Column: Top Input Bar + Trackpad Surface
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          // Pill input bar
          TrackpadInputBar(
            inputMode = inputMode,
            inputText = inputText,
            isPrivacyMode = isPrivacyMode,
            showModeMenu = showModeMenu,
            onModeToggleClick = { showModeMenu = !showModeMenu },
            onDismissModeMenu = { showModeMenu = false },
            onSelectMode = { mode ->
              inputMode = mode
              showModeMenu = false
            },
            onInputTextChange = { inputText = it },
            onSend = {
              if (inputText.isNotBlank()) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                triggerKeyBadge(inputText)
                if (inputMode == TrackpadInputMode.PURE_TEXT) {
                  onSendText(inputText)
                } else {
                  onSendShortcut(inputText)
                }
                inputText = ""
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Trackpad Box with restored outer intelligence glow
          TrackpadBox(
            isPointerActive = isPointerActive,
            isPrivacyMode = isPrivacyMode,
            floatingKeyBadge = lastCanvasKeyBadge,
            onActivatePointer = { togglePointerActive() },
            onMouseMove = onMouseMove,
            onMouseClick = { button ->
              triggerKeyBadge(button)
              onMouseClick(button)
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Right Column: Title + Mathematically Centered Play/Pause Button + Back Button
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Trackpad",
            color = PortageText,
            fontSize = 20.sp,
            fontFamily = HeadingFontFamily,
            modifier = Modifier
              .align(Alignment.TopCenter)
              .testTag("trackpad_title_landscape")
          )

          // Play button is blue CTA; when played, pause button is our red
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(54.dp)
              .clip(CircleShape)
              .background(if (isPointerActive) PortageDanger else PortagePrimary)
              .border(
                1.5.dp,
                if (isPointerActive) Color(0xFFF87171) else Color(0xFF60A5FA),
                CircleShape
              )
              .clickable { togglePointerActive() }
              .testTag("trackpad_play_button_landscape"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPointerActive) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPointerActive) "Pause Pointer" else "Activate Pointer",
              tint = Color.White,
              modifier = Modifier.size(26.dp)
            )
          }

          // Back Button
          IconButton(
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              onBackClick()
            },
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .testTag("trackpad_back_button_landscape")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PortageText,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    } else {
      // Portrait Layout
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        // Large Trackpad Box with restored outer intelligence glow
        TrackpadBox(
          isPointerActive = isPointerActive,
          isPrivacyMode = isPrivacyMode,
          floatingKeyBadge = lastCanvasKeyBadge,
          onActivatePointer = { togglePointerActive() },
          onMouseMove = onMouseMove,
          onMouseClick = { button ->
            triggerKeyBadge(button)
            onMouseClick(button)
          },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Pill Input Bar (Pure text or Shortcut mode with password masking under Privacy Mode)
        TrackpadInputBar(
          inputMode = inputMode,
          inputText = inputText,
          isPrivacyMode = isPrivacyMode,
          showModeMenu = showModeMenu,
          onModeToggleClick = { showModeMenu = !showModeMenu },
          onDismissModeMenu = { showModeMenu = false },
          onSelectMode = { mode ->
            inputMode = mode
            showModeMenu = false
          },
          onInputTextChange = { inputText = it },
          onSend = {
            if (inputText.isNotBlank()) {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              triggerKeyBadge(inputText)
              if (inputMode == TrackpadInputMode.PURE_TEXT) {
                onSendText(inputText)
              } else {
                onSendShortcut(inputText)
              }
              inputText = ""
            }
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Bar: Back Button, Mathematically Centered Play/Pause Button, "Trackpad" Label
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(bottom = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          // Left: Back button anchored to Start
          IconButton(
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              onBackClick()
            },
            modifier = Modifier
              .align(Alignment.CenterStart)
              .testTag("trackpad_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PortageText,
              modifier = Modifier.size(24.dp)
            )
          }

          // Center: Play/Pause button precisely at exact horizontal and vertical center
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(54.dp)
              .clip(CircleShape)
              .background(if (isPointerActive) PortageDanger else PortagePrimary)
              .border(
                1.5.dp,
                if (isPointerActive) Color(0xFFF87171) else Color(0xFF60A5FA),
                CircleShape
              )
              .clickable { togglePointerActive() }
              .testTag("trackpad_play_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPointerActive) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPointerActive) "Pause Pointer" else "Activate Pointer",
              tint = Color.White,
              modifier = Modifier.size(26.dp)
            )
          }

          // Right: "Trackpad" in Heading Serif font anchored to End
          Text(
            text = "Trackpad",
            color = PortageText,
            fontSize = 20.sp,
            fontFamily = HeadingFontFamily,
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .padding(end = 8.dp)
              .testTag("trackpad_title")
          )
        }
      }
    }
  }
}

/**
 * Large Trackpad surface with:
 * - Restored Apple Intelligence glow intensity strictly OUTSIDE the box perimeter,
 *   wrapping both the upper touchpad and the bottom click buttons together.
 * - Localized touch dot glow: dots glow only where the finger touches in that area.
 * - Live floating key press badges at top of canvas (masked with "•" under Privacy Mode).
 * - Subtle "[EyeOff] Privacy Mode" indicator badge at top-right corner while capture is active.
 * - Touchpad motion enabled when pointer is active; deactivated when inactive.
 * - Bottom physical Left/Right click zones.
 */
@Composable
private fun TrackpadBox(
  isPointerActive: Boolean,
  isPrivacyMode: Boolean,
  floatingKeyBadge: String?,
  onActivatePointer: () -> Unit,
  onMouseMove: (Float, Float) -> Unit,
  onMouseClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current
  var touchPosition by remember { mutableStateOf<Offset?>(null) }

  // Restored Apple Intelligence glow animations
  val infiniteTransition = rememberInfiniteTransition(label = "outer_glow")
  val angle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "glow_angle"
  )
  val breathe0s by infiniteTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_breathe_0s"
  )
  val breathe02s by infiniteTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, delayMillis = 200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_breathe_02s"
  )

  val glowPaint = remember {
    Paint().apply {
      isAntiAlias = true
      style = Paint.Style.STROKE
    }
  }

  // Wrapper Box: renders restored Apple Intelligence glow layers OUTSIDE the card
  Box(
    modifier = modifier
      .padding(4.dp)
      .drawBehind {
        if (isPointerActive) {
          val canvas = drawContext.canvas.nativeCanvas
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val cy = h / 2f
          val rPx = 24.dp.toPx()

          // Conic sweep gradient rotating outside around the box perimeter
          val sweep = SweepGradient(cx, cy, APPLE_INTELLIGENCE_COLORS, null)
          val matrix = android.graphics.Matrix()
          matrix.postRotate(angle, cx, cy)
          sweep.setLocalMatrix(matrix)
          glowPaint.shader = sweep

          // Render multi-layer restored radiant glow layers projecting outside
          for (layer in OUTER_GLOW_LAYERS) {
            val breatheFactor = if (layer.breathe) {
              if (layer.delayMs > 0) breathe02s else breathe0s
            } else {
              1.0f
            }
            val currentOpacity = layer.o * breatheFactor
            val alpha = (currentOpacity * 255f).toInt().coerceIn(0, 255)
            val tPx = layer.t.dp.toPx()
            val bPx = layer.b.dp.toPx()

            glowPaint.alpha = alpha
            glowPaint.strokeWidth = tPx
            glowPaint.maskFilter = BlurMaskFilter(bPx, BlurMaskFilter.Blur.NORMAL)

            val outPx = tPx / 2f
            val rect = RectF(
              -outPx,
              -outPx,
              w + outPx,
              h + outPx
            )
            val adjustedR = rPx + outPx
            canvas.drawRoundRect(rect, adjustedR, adjustedR, glowPaint)
          }

          // Subtle ambient outer border highlight
          glowPaint.maskFilter = null
          glowPaint.alpha = 100
          glowPaint.strokeWidth = 1.dp.toPx()
          val borderRect = RectF(
            -0.5.dp.toPx(),
            -0.5.dp.toPx(),
            w + 0.5.dp.toPx(),
            h + 0.5.dp.toPx()
          )
          canvas.drawRoundRect(borderRect, rPx + 0.5.dp.toPx(), rPx + 0.5.dp.toPx(), glowPaint)
        }
      }
      .clip(RoundedCornerShape(24.dp))
      .background(PortageSurface2)
      .border(
        width = 1.dp,
        color = if (isPointerActive) Color(0xFF8D9FFF).copy(alpha = 0.35f) else PortageBorder,
        shape = RoundedCornerShape(24.dp)
      )
      .testTag("trackpad_box")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Touchpad Surface with Dotted Grid
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .pointerInput(isPointerActive) {
            if (!isPointerActive) {
              detectTapGestures(
                onTap = {
                  onActivatePointer()
                }
              )
            } else {
              detectTapGestures(
                onPress = { offset ->
                  touchPosition = offset
                  tryAwaitRelease()
                  touchPosition = null
                },
                onTap = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onMouseClick("LEFT")
                },
                onDoubleTap = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onMouseClick("DOUBLE")
                },
                onLongPress = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onMouseClick("RIGHT")
                }
              )
            }
          }
          .pointerInput(isPointerActive) {
            if (!isPointerActive) return@pointerInput
            detectDragGestures(
              onDragStart = { offset ->
                touchPosition = offset
              },
              onDrag = { change, dragAmount ->
                change.consume()
                touchPosition = change.position
                onMouseMove(dragAmount.x * 1.35f, dragAmount.y * 1.35f)
              },
              onDragEnd = {
                touchPosition = null
              },
              onDragCancel = {
                touchPosition = null
              }
            )
          }
          .testTag("trackpad_touch_surface")
      ) {
        // Draw subtle dot grid texture + localized glowing dots where touched
        Canvas(modifier = Modifier.fillMaxSize()) {
          val baseDotRadius = 1.3f
          val step = 20.dp.toPx()
          val defaultDotColor = Color.White.copy(alpha = 0.08f)
          val glowRadius = 55.dp.toPx()
          val touch = touchPosition

          var x = step
          while (x < size.width) {
            var y = step
            while (y < size.height) {
              if (isPointerActive && touch != null) {
                val dx = x - touch.x
                val dy = y - touch.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < glowRadius) {
                  val intensity = (1f - (dist / glowRadius)).coerceIn(0f, 1f)
                  val activeRadius = baseDotRadius + 2.2f * intensity
                  val dotColor = Color(
                    red = (0.22f + 0.35f * intensity).coerceIn(0f, 1f),
                    green = (0.65f + 0.30f * intensity).coerceIn(0f, 1f),
                    blue = 1.0f,
                    alpha = (0.25f + 0.75f * intensity).coerceIn(0f, 1f)
                  )
                  // Radiant soft light aura under the dot
                  drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.32f * intensity),
                    radius = activeRadius * 2.8f,
                    center = Offset(x, y)
                  )
                  // Bright glowing dot
                  drawCircle(
                    color = dotColor,
                    radius = activeRadius,
                    center = Offset(x, y)
                  )
                } else {
                  drawCircle(
                    color = defaultDotColor,
                    radius = baseDotRadius,
                    center = Offset(x, y)
                  )
                }
              } else {
                drawCircle(
                  color = defaultDotColor,
                  radius = baseDotRadius,
                  center = Offset(x, y)
                )
              }
              y += step
            }
            x += step
          }

          // Draw radiant light bloom at the exact touch position
          if (isPointerActive && touch != null) {
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(
                  Color(0xFF38BDF8).copy(alpha = 0.38f),
                  Color(0xFF818CF8).copy(alpha = 0.18f),
                  Color.Transparent
                ),
                center = touch,
                radius = glowRadius
              ),
              center = touch,
              radius = glowRadius
            )
          }
        }

        // Live floating key press badge at the top of the canvas (masked with "•" under Privacy Mode)
        androidx.compose.animation.AnimatedVisibility(
          visible = floatingKeyBadge != null,
          enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { -it / 2 },
          exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { -it / 2 },
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 16.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E1E22).copy(alpha = 0.95f))
              .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .testTag("trackpad_floating_key_badge")
          ) {
            Text(
              text = PrivacyModeManager.formatKey(floatingKeyBadge, isPrivacyMode),
              color = PortageText,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = GeistFontFamily
            )
          }
        }

        // Subtle "[EyeOff] Privacy Mode" indicator badge at the top-right corner of the canvas
        PrivacyModeCanvasBadge(
          visible = true,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(14.dp)
        )

        // Deactivated overlay badge when pointer is disabled
        if (!isPointerActive) {
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .clip(RoundedCornerShape(999.dp))
              .background(Color(0xFF141417).copy(alpha = 0.90f))
              .border(1.dp, PortageBorder, RoundedCornerShape(999.dp))
              .clickable { onActivatePointer() }
              .padding(horizontal = 16.dp, vertical = 9.dp)
              .testTag("pointer_inactive_pill"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF59E0B))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Pointer Inactive • Tap Play to Control",
                color = PortageTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GeistFontFamily
              )
            }
          }
        }
      }

      // Horizontal Divider between touchpad and bottom click bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(PortageBorder)
      )

      // Bottom Click Buttons: Left Click & Right Click (wrapped inside the same outer glow)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(68.dp)
          .background(Color(0xFF141416))
      ) {
        val leftInteractionSource = remember { MutableInteractionSource() }
        val isLeftPressed by leftInteractionSource.collectIsPressedAsState()

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (isLeftPressed) PortageSurface3 else Color.Transparent)
            .clickable(
              interactionSource = leftInteractionSource,
              indication = null
            ) {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              if (isPointerActive) {
                onMouseClick("LEFT")
              } else {
                onActivatePointer()
              }
            }
            .testTag("trackpad_left_click_button"),
          contentAlignment = Alignment.Center
        ) {
          // Clean physical button feel
        }

        // Vertical Center Divider between Left and Right Buttons
        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(PortageBorder)
        )

        val rightInteractionSource = remember { MutableInteractionSource() }
        val isRightPressed by rightInteractionSource.collectIsPressedAsState()

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (isRightPressed) PortageSurface3 else Color.Transparent)
            .clickable(
              interactionSource = rightInteractionSource,
              indication = null
            ) {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              if (isPointerActive) {
                onMouseClick("RIGHT")
              } else {
                onActivatePointer()
              }
            }
            .testTag("trackpad_right_click_button"),
          contentAlignment = Alignment.Center
        ) {
          // Clean physical button feel
        }
      }
    }
  }
}

/**
 * Capsule Pill Input Bar with Pure Text / Shortcut switcher, text field with password masking, and Send button.
 */
@Composable
private fun TrackpadInputBar(
  inputMode: TrackpadInputMode,
  inputText: String,
  isPrivacyMode: Boolean = false,
  showModeMenu: Boolean,
  onModeToggleClick: () -> Unit,
  onDismissModeMenu: () -> Unit,
  onSelectMode: (TrackpadInputMode) -> Unit,
  onInputTextChange: (String) -> Unit,
  onSend: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current

  Row(
    modifier = modifier
      .height(58.dp)
      .clip(CircleShape)
      .background(Color(0xFF1C1C1F))
      .border(1.dp, PortageBorder, CircleShape)
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("trackpad_input_bar"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Mode Switcher Pill: [ T ˅ ] or [ ⌘ ˅ ]
    Box {
      Row(
        modifier = Modifier
          .clip(CircleShape)
          .background(Color(0xFF111113))
          .border(1.dp, Color(0xFF2C2C32), CircleShape)
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onModeToggleClick()
          }
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("input_mode_toggle"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (inputMode == TrackpadInputMode.PURE_TEXT) "T" else "⌘",
          color = PortageText,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = GeistFontFamily
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "Switch Input Mode",
          tint = PortageTextMuted,
          modifier = Modifier.size(16.dp)
        )
      }

      DropdownMenu(
        expanded = showModeMenu,
        onDismissRequest = onDismissModeMenu,
        modifier = Modifier
          .background(PortageSurface2)
          .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
      ) {
        DropdownMenuItem(
          text = {
            Text(
              text = "T  Pure Text",
              color = if (inputMode == TrackpadInputMode.PURE_TEXT) PortagePrimary else PortageText,
              fontFamily = GeistFontFamily,
              fontWeight = if (inputMode == TrackpadInputMode.PURE_TEXT) FontWeight.Bold else FontWeight.Normal
            )
          },
          onClick = { onSelectMode(TrackpadInputMode.PURE_TEXT) },
          modifier = Modifier.testTag("menu_mode_pure_text")
        )
        DropdownMenuItem(
          text = {
            Text(
              text = "⌘  Shortcut Key",
              color = if (inputMode == TrackpadInputMode.SHORTCUT_KEY) PortagePrimary else PortageText,
              fontFamily = GeistFontFamily,
              fontWeight = if (inputMode == TrackpadInputMode.SHORTCUT_KEY) FontWeight.Bold else FontWeight.Normal
            )
          },
          onClick = { onSelectMode(TrackpadInputMode.SHORTCUT_KEY) },
          modifier = Modifier.testTag("menu_mode_shortcut_key")
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Text Field (masked as password dots when isPrivacyMode is true)
    Box(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (inputText.isEmpty()) {
        Text(
          text = if (inputMode == TrackpadInputMode.PURE_TEXT) {
            "Type a text like \"hello\""
          } else {
            "Type a shortcut like \"ctrl+c\""
          },
          color = PortageTextMuted,
          fontSize = 14.sp,
          fontFamily = GeistFontFamily
        )
      }

      BasicTextField(
        value = inputText,
        onValueChange = onInputTextChange,
        visualTransformation = if (isPrivacyMode) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = TextStyle(
          color = PortageText,
          fontSize = 14.sp,
          fontFamily = GeistFontFamily
        ),
        cursorBrush = SolidColor(PortagePrimary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("trackpad_input_field")
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Privacy Mode toggle button placed directly inside the field bar
    val context = LocalContext.current
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(if (isPrivacyMode) PortagePrimary.copy(alpha = 0.18f) else Color(0xFF242429))
        .border(
          width = 1.dp,
          color = if (isPrivacyMode) PortagePrimary.copy(alpha = 0.6f) else Color(0xFF33333A),
          shape = CircleShape
        )
        .clickable {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          PrivacyModeManager.toggle(context)
        }
        .testTag("trackpad_field_privacy_toggle"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
        contentDescription = if (isPrivacyMode) "Privacy Mode On" else "Privacy Mode Off",
        tint = if (isPrivacyMode) PortagePrimary else PortageTextMuted,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Blue Circular Send Button
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(PortagePrimary)
        .clickable { onSend() }
        .testTag("trackpad_send_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Send,
        contentDescription = "Send",
        tint = Color.White,
        modifier = Modifier.size(17.dp)
      )
    }
  }
}
