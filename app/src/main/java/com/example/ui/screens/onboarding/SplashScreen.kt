package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.OnestFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onStartClick: () -> Unit,
  onAutoNavigateHome: () -> Unit,
  isAlreadyOnboarded: Boolean
) {
  var showButton by remember { mutableStateOf(false) }

  LaunchedEffect(isAlreadyOnboarded) {
    if (isAlreadyOnboarded) {
      delay(800)
      onAutoNavigateHome()
    } else {
      delay(1200)
      showButton = true
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .navigationBarsPadding()
      .testTag("splash_screen")
  ) {
    // Centered wordmark and tagline (remains stationary)
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Portage",
        color = PortageText,
        fontSize = 54.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = OnestFontFamily,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "CONNECT AND CONTROL YOUR DEVICES",
        color = PortageTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = GeistFontFamily,
        letterSpacing = 1.2.sp,
        textAlign = TextAlign.Center
      )
    }

    // Start Now button slides and fades in after 1.2s
    AnimatedVisibility(
      visible = showButton,
      enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
      PortagePrimaryButton(
        text = "Start Now",
        onClick = onStartClick,
        height = 60.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "start_now_button"
      )
    }
  }
}
