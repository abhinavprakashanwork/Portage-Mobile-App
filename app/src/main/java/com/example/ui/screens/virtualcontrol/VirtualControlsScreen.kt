package com.example.ui.screens.virtualcontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun VirtualControlsScreen(
  onBackClick: () -> Unit,
  onNavigateToTrackpad: () -> Unit
) {
  val haptics = LocalHapticFeedback.current

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("virtual_controls_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp)
    ) {
      // Top bar title: "Portage" in center, back button on left
      PortageTopBar(
        showBackButton = true,
        onBackClick = onBackClick,
        showNotificationButton = false
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Header: "Virtual Controls" in large Serif font
      Text(
        text = "Virtual Controls",
        color = PortageText,
        fontSize = 34.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = HeadingFontFamily,
        letterSpacing = (-0.5).sp,
        modifier = Modifier.testTag("virtual_controls_heading")
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Simulated controls that follows physical hardware look",
        color = PortageTextMuted,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = GeistFontFamily
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Option Card: Trackpad
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(PortageSurface2)
          .border(1.dp, PortageBorder, RoundedCornerShape(18.dp))
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToTrackpad()
          }
          .padding(horizontal = 18.dp)
          .testTag("option_trackpad"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CropLandscape,
            contentDescription = "Trackpad",
            tint = PortageText,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.size(14.dp))
          Text(
            text = "Trackpad",
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GeistFontFamily
          )
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Open Trackpad",
          tint = PortageTextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
