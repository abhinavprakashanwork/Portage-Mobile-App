package com.example.ui.screens.liverelay

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.PrivacyModeManager
import com.example.ui.components.HardwareKeyInterceptor
import com.example.ui.components.IntelligenceAudioFeedback
import com.example.ui.components.IntelligenceGlowOverlay
import com.example.ui.components.PortageTopBar
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TactileKeyBadge
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSuccess
import com.example.ui.theme.PortageSurface1
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class RelayedKeyItem(
  val id: String = UUID.randomUUID().toString(),
  val key: String
)

@Composable
fun LiveRelayScreen(
  isConnected: Boolean,
  onBackClick: () -> Unit,
  onNotificationClick: () -> Unit = {},
  onRelayKey: suspend (String) -> Boolean
) {
  var isCapturing by remember { mutableStateOf(false) }
  var manualShortcut by remember { mutableStateOf("") }
  val capturedKeys = remember { mutableStateListOf<RelayedKeyItem>() }
  var lastSentFlyout by remember { mutableStateOf<String?>(null) }
  var flyoutJob by remember { mutableStateOf<Job?>(null) }

  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()
  val suggestions = remember {
    listOf("Vol +", "Vol -", "Ctrl + C", "Ctrl + V", "Ctrl + Z", "Ctrl + A", "Alt + Tab", "Win + D", "Esc", "Enter", "Space")
  }

  fun relayAndAddKey(key: String) {
    val newId = "${System.currentTimeMillis()}-${(1000..9999).random()}"
    val item = RelayedKeyItem(id = newId, key = key)

    // Capped at latest 14 items
    while (capturedKeys.size >= 14) {
      capturedKeys.removeAt(0)
    }
    capturedKeys.add(item)

    // Update bottom flyout of what has been sent
    lastSentFlyout = key
    flyoutJob?.cancel()
    flyoutJob = scope.launch {
      delay(2500)
      lastSentFlyout = null
    }

    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    scope.launch {
      onRelayKey(key)
      // Automatically fade out and remove after 2000ms
      delay(2000)
      capturedKeys.remove(item)
    }
  }

  // Intercept hardware volume keys with priority
  HardwareKeyInterceptor()

  // Listen to physical hardware volume keys bus
  LaunchedEffect(Unit) {
    com.example.service.VolumeKeyBus.keyEvents.collect { key ->
      relayAndAddKey(key)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("live_relay_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
      // Top bar with back button + uniform notification button
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = true,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header
      ScreenHeader(
        title = "Live Relay",
        subtitle = "Type or enter a short-cut that can be relayed in real-time."
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Captures bar: height 58, bg --surface-1, 1px border, radius 20
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(PortageSurface1)
          .border(1.dp, PortageBorderSubtle, RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Label is explicitly normal weight (not bold)
          Text(
            text = "CAPTURES",
            color = PortageTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.width(10.dp))

          if (capturedKeys.isEmpty()) {
            Text(
              text = if (isCapturing) "Listening for keys…" else "Inactive",
              color = PortageTextMuted,
              fontSize = 12.sp,
              fontFamily = GeistFontFamily,
              fontWeight = FontWeight.Normal
            )
          } else {
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              items(capturedKeys, key = { it.id }) { item ->
                Box(
                  modifier = Modifier.animateItem(
                    fadeInSpec = tween(durationMillis = 200),
                    fadeOutSpec = tween(durationMillis = 350),
                    placementSpec = tween(durationMillis = 200)
                  )
                ) {
                  AnimatedKeyBadge(label = PrivacyModeManager.formatKey(item.key, isPrivacyMode))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Privacy Mode eye toggle button on right side of CAPTURES toolbar
        IconButton(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            PrivacyModeManager.toggle(context)
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("privacy_mode_toggle_captures")
        ) {
          Icon(
            imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = "Privacy Mode",
            tint = if (isPrivacyMode) PortagePrimary else PortageTextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Desktop-style Capture Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(24.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(24.dp))
          .testTag("capture_area"),
        contentAlignment = Alignment.Center
      ) {
        // Dot grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
          val dotColor = Color(0xFF383838)
          val step = 28.dp.toPx()
          val radius = 1.2.dp.toPx()
          var x = step / 2
          while (x < size.width) {
            var y = step / 2
            while (y < size.height) {
              drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(x, y)
              )
              y += step
            }
            x += step
          }
        }

        // Apple Intelligence Inward Perimeter Glow Overlay (strictly inside the container)
        IntelligenceGlowOverlay(
          active = isCapturing,
          cornerRadius = 24.dp
        )

        // Center content:
        // "Live capturing is not active / active" in Instrument Serif (NOT bold)
        // "Your inputs are being captured." in Geist (NOT bold)
        // [ Start Capture / Stop Capture ] rounded rectangle button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(24.dp)
        ) {
          Text(
            text = if (!isCapturing) "Live capturing is not active" else "Live capturing is active",
            color = PortageText,
            fontFamily = InstrumentSerifFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Your inputs are being captured.",
            color = PortageTextMuted,
            fontFamily = GeistFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              if (!isCapturing) {
                if (!isConnected) {
                  Toast.makeText(context, "Device not connected", Toast.LENGTH_SHORT).show()
                } else {
                  isCapturing = true
                  IntelligenceAudioFeedback.playChime(true)
                }
              } else {
                isCapturing = false
                IntelligenceAudioFeedback.playChime(false)
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (!isCapturing) PortagePrimary else Color(0xFFB91C1C),
              contentColor = PortageText
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
              .height(44.dp)
              .testTag(if (!isCapturing) "start_capture_button" else "stop_capture_button")
          ) {
            Text(
              text = if (!isCapturing) "Start Capture" else "Stop Capture",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = GeistFontFamily
            )
          }
        }

        // Flyout at the bottom of the box showing what has been sent
        androidx.compose.animation.AnimatedVisibility(
          visible = lastSentFlyout != null,
          enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(250)) { it / 2 },
          exit = fadeOut(animationSpec = tween(280)) + slideOutVertically(animationSpec = tween(280)) { it / 2 },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
            .testTag("sent_flyout_badge")
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
              .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .clip(CircleShape)
                  .background(PortageSuccess)
              )
              Text(
                text = "Sent: ${PrivacyModeManager.formatKey(lastSentFlyout, isPrivacyMode)}",
                color = PortageText,
                fontSize = 12.sp,
                fontFamily = GeistFontFamily,
                fontWeight = FontWeight.Normal
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Shortcut suggestions pills
      Text(
        text = "QUICK RELAY SUGGESTIONS",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(suggestions) { shortcut ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(PortageSurface2)
              .border(1.dp, PortageBorder, RoundedCornerShape(10.dp))
              .clickable {
                relayAndAddKey(shortcut)
              }
              .padding(horizontal = 12.dp, vertical = 7.dp)
          ) {
            Text(
              text = shortcut,
              color = PortageText,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Input row: manual shortcut input + Send button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PortageSurface3)
            .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          BasicTextField(
            value = manualShortcut,
            onValueChange = { manualShortcut = it },
            textStyle = TextStyle(
              color = PortageText,
              fontSize = 14.sp,
              fontFamily = GeistFontFamily
            ),
            cursorBrush = SolidColor(PortageText),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("manual_shortcut_input"),
            decorationBox = { innerTextField ->
              if (manualShortcut.isEmpty()) {
                Text(
                  text = "Type shortcut (e.g. Ctrl+Shift+Esc)…",
                  color = PortageTextMuted,
                  fontSize = 13.sp
                )
              }
              innerTextField()
            }
          )
        }

        Button(
          onClick = {
            if (manualShortcut.isNotBlank()) {
              relayAndAddKey(manualShortcut.trim())
              manualShortcut = ""
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PortagePrimary,
            contentColor = PortageText,
            disabledContainerColor = PortagePrimary.copy(alpha = 0.4f),
            disabledContentColor = PortageText.copy(alpha = 0.5f)
          ),
          enabled = manualShortcut.isNotBlank(),
          contentPadding = PaddingValues(horizontal = 18.dp),
          modifier = Modifier
            .height(52.dp)
            .testTag("send_shortcut_button")
        ) {
          Text(
            text = "Send",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

/**
 * Animated keycap badge matching user's exact Framer Motion animation spec:
 * - pops in slightly smaller (scale 0.85 -> 1.0) and from below (y 4 -> 0) over 0.2s
 * - tactile physical keycap styling (bg #4B4B4B, border white/10, 11sp semibold text)
 */
@Composable
private fun AnimatedKeyBadge(
  label: String,
  modifier: Modifier = Modifier
) {
  var isAppeared by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    isAppeared = true
  }

  val scale by animateFloatAsState(
    targetValue = if (isAppeared) 1f else 0.85f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "scale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (isAppeared) 1f else 0f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "alpha"
  )
  val translateY by animateFloatAsState(
    targetValue = if (isAppeared) 0f else 4f,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "translateY"
  )

  Box(
    modifier = modifier
      .graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
        this.alpha = alpha
        this.translationY = translateY
      }
  ) {
    TactileKeyBadge(text = label)
  }
}
