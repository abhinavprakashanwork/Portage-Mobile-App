package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.PrivacyModeManager
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

/**
 * Standard eye toggle button for Privacy Mode.
 * Eye (Visibility) when inactive, EyeOff (VisibilityOff) when active.
 */
@Composable
fun PrivacyModeToggleButton(
  modifier: Modifier = Modifier,
  tint: Color? = null
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()

  IconButton(
    onClick = {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      PrivacyModeManager.toggle(context)
    },
    modifier = modifier.testTag("privacy_mode_toggle_button")
  ) {
    Icon(
      imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
      contentDescription = "Privacy Mode",
      tint = tint ?: if (isPrivacyMode) PortagePrimary else PortageTextMuted,
      modifier = Modifier.size(20.dp)
    )
  }
}

/**
 * Pill styled toggle with eye icon and label for toolbars or action bars.
 */
@Composable
fun PrivacyModePill(
  modifier: Modifier = Modifier,
  showLabel: Boolean = true
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(if (isPrivacyMode) PortagePrimary.copy(alpha = 0.15f) else PortageSurface2)
      .border(
        width = 1.dp,
        color = if (isPrivacyMode) PortagePrimary.copy(alpha = 0.6f) else PortageBorder,
        shape = RoundedCornerShape(999.dp)
      )
      .clickable {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        PrivacyModeManager.toggle(context)
      }
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("privacy_mode_pill"),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
        contentDescription = "Privacy Mode",
        tint = if (isPrivacyMode) PortagePrimary else PortageTextMuted,
        modifier = Modifier.size(16.dp)
      )
      if (showLabel) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isPrivacyMode) "Privacy On" else "Privacy",
          color = if (isPrivacyMode) PortagePrimary else PortageText,
          fontSize = 12.sp,
          fontWeight = if (isPrivacyMode) FontWeight.SemiBold else FontWeight.Normal,
          fontFamily = GeistFontFamily
        )
      }
    }
  }
}

/**
 * Subtle top-right corner badge displayed on the interactive canvas when Privacy Mode is active.
 */
@Composable
fun PrivacyModeCanvasBadge(
  modifier: Modifier = Modifier,
  visible: Boolean = true
) {
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()

  AnimatedVisibility(
    visible = visible && isPrivacyMode,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(999.dp))
        .background(Color(0xFF141417).copy(alpha = 0.88f))
        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(999.dp))
        .padding(horizontal = 10.dp, vertical = 5.dp)
        .testTag("privacy_mode_canvas_badge"),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.VisibilityOff,
          contentDescription = "Privacy Mode Active",
          tint = PortagePrimary,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "Privacy Mode",
          color = PortageTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = GeistFontFamily
        )
      }
    }
  }
}
