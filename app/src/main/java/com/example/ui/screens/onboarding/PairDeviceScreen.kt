package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HelpLink
import com.example.ui.components.HelpSheet
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageDangerBg
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSuccess
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import com.example.ui.theme.PortageTextSubtitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PairDeviceScreen(
  onPaired: () -> Unit,
  onBackClick: () -> Unit
) {
  var pairingKey by remember { mutableStateOf("") }
  var isPairing by remember { mutableStateOf(false) }
  var isPairedSuccess by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showHelpSheet by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current
  val haptics = LocalHapticFeedback.current

  fun handlePair() {
    focusManager.clearFocus()
    val trimmed = pairingKey.trim()

    if (trimmed.isEmpty()) {
      errorMessage = "Pairing key is required"
      return
    }

    // Invalid test keys or codes shorter than 4 digits trigger the invalid key error state
    if (trimmed == "0000" || trimmed.equals("invalid", ignoreCase = true) || trimmed.length < 4) {
      errorMessage = "Invalid pairing key. Please verify the code and try again."
      return
    }

    // Begin pairing process (loading state)
    errorMessage = null
    isPairing = true

    scope.launch {
      delay(1400) // Simulating secure pairing handshake with Portage device
      isPairing = false
      isPairedSuccess = true
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      delay(500)
      onPaired()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding()
      .testTag("pair_device_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
      // Top bar with back button
      PortageTopBar(
        showBackButton = true,
        onBackClick = onBackClick,
        showNotificationButton = false
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Header Title: "Pair your device" in Instrument Serif
      Text(
        text = "Pair your device",
        color = PortageText,
        fontSize = 38.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = InstrumentSerifFontFamily,
        letterSpacing = (-0.5).sp,
        lineHeight = 42.sp,
        modifier = Modifier.testTag("pair_device_title")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Short, simple explanation that a pairing key is required
      Text(
        text = "A pairing key is required to securely pair and authenticate your Portage device.",
        color = PortageTextSubtitle,
        fontSize = 14.sp,
        fontFamily = GeistFontFamily,
        lineHeight = 20.sp,
        modifier = Modifier.testTag("pair_device_subtitle")
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Security & Connection Panel (matches device_panel from ConnectDeviceScreen with equal height)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
          .testTag("pair_device_panel"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          // Status Pill: Device Connected
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(PortageSurface2)
              .border(1.dp, PortageBorder, CircleShape)
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isPairedSuccess) PortageSuccess else PortagePrimary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isPairedSuccess) "Device Paired" else "Portage Device Detected",
                color = PortageText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GeistFontFamily
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Lock Graphic
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(
                if (errorMessage != null) PortageDangerBg
                else if (isPairedSuccess) PortageSuccess.copy(alpha = 0.15f)
                else PortagePrimary.copy(alpha = 0.15f)
              )
              .border(
                1.dp,
                if (errorMessage != null) PortageDanger.copy(alpha = 0.4f)
                else if (isPairedSuccess) PortageSuccess.copy(alpha = 0.5f)
                else PortagePrimary.copy(alpha = 0.4f),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isPairing) {
              CircularProgressIndicator(
                color = PortagePrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
              )
            } else if (isPairedSuccess) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = PortageSuccess,
                modifier = Modifier.size(32.dp)
              )
            } else {
              Icon(
                imageVector = if (errorMessage != null) Icons.Default.ErrorOutline else Icons.Default.Lock,
                contentDescription = "Security",
                tint = if (errorMessage != null) PortageDanger else PortagePrimary,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = if (isPairing) "Verifying security credentials…"
            else if (isPairedSuccess) "Device paired successfully!"
            else if (errorMessage != null) "Pairing failed"
            else "Secure Hardware Handshake",
            color = if (errorMessage != null) PortageDanger else PortageText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = GeistFontFamily,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = if (isPairing) "Please wait while we establish an encrypted link."
            else if (errorMessage != null) "The provided pairing key was rejected by the device."
            else "Enter the 6-digit pairing key shown on your target device.",
            color = PortageTextMuted,
            fontSize = 13.sp,
            fontFamily = GeistFontFamily,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Pairing Key Input Field Label
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pairing key",
          color = PortageTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = GeistFontFamily
        )

        // Helper hint tag (tap to fill default)
        Text(
          text = "Default key: 123456",
          color = PortagePrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = GeistFontFamily,
          modifier = Modifier.clickable {
            pairingKey = "123456"
            errorMessage = null
          }
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Pairing Key Input Field
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .border(
            width = 1.dp,
            color = if (errorMessage != null) PortageDanger else if (isPairing) PortagePrimary else PortageBorder,
            shape = RoundedCornerShape(16.dp)
          )
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        BasicTextField(
          value = pairingKey,
          onValueChange = {
            if (it.length <= 16) {
              pairingKey = it
              if (errorMessage != null) errorMessage = null
            }
          },
          enabled = !isPairing && !isPairedSuccess,
          textStyle = TextStyle(
            color = PortageText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
          ),
          cursorBrush = SolidColor(PortagePrimary),
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = { handlePair() }),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pairing_key_input"),
          decorationBox = { innerTextField ->
            if (pairingKey.isEmpty()) {
              Text(
                text = "e.g. 123456",
                color = PortageTextMuted,
                fontSize = 15.sp,
                fontFamily = GeistFontFamily,
                letterSpacing = 0.sp
              )
            }
            innerTextField()
          }
        )
      }

      // Error state for invalid pairing key
      AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Column {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("pair_error_message")
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Error",
              tint = PortageDanger,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = errorMessage ?: "",
              color = PortageDanger,
              fontSize = 13.sp,
              fontFamily = GeistFontFamily
            )
          }
        }
      }

      // Spacer with weight(1f) to push the button cleanly to the bottom
      Spacer(modifier = Modifier.weight(1f))

      // Primary Button: "Pair device" pinned to bottom
      PortagePrimaryButton(
        text = if (isPairing) "Pairing…" else if (isPairedSuccess) "Paired" else "Pair device",
        onClick = { handlePair() },
        enabled = pairingKey.isNotBlank() && !isPairing && !isPairedSuccess,
        height = 60.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "pair_device_button"
      )

      Spacer(modifier = Modifier.height(8.dp))

      HelpLink(onClick = { showHelpSheet = true })

      Spacer(modifier = Modifier.height(16.dp))
    }

    if (showHelpSheet) {
      HelpSheet(onDismiss = { showHelpSheet = false })
    }
  }
}
