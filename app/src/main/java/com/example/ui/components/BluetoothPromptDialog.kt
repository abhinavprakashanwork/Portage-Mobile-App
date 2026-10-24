package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import com.example.ui.theme.PortageTextSubtitle

@Composable
fun BluetoothPromptDialog(
  onDismiss: () -> Unit,
  onEnableBluetooth: () -> Unit
) {
  val haptics = LocalHapticFeedback.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 380.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(PortageSurface2)
        .border(1.dp, PortageBorder, RoundedCornerShape(24.dp))
        .padding(24.dp)
        .testTag("bluetooth_prompt_dialog")
    ) {
      // Close 'X' button in top-right
      IconButton(
        onClick = {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          onDismiss()
        },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(32.dp)
          .testTag("bluetooth_dialog_close")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = PortageTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Bluetooth Badge
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(PortagePrimary.copy(alpha = 0.15f))
            .border(1.dp, PortagePrimary.copy(alpha = 0.40f), CircleShape)
            .testTag("bluetooth_icon_badge"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = "Bluetooth",
            tint = PortagePrimary,
            modifier = Modifier.size(34.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title in Instrument Serif
        Text(
          text = "Turn on Bluetooth",
          color = PortageText,
          fontSize = 28.sp,
          fontWeight = FontWeight.Normal,
          fontFamily = InstrumentSerifFontFamily,
          letterSpacing = (-0.5).sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("bluetooth_dialog_title")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Explanation text
        Text(
          text = "Portage requires Bluetooth to discover, securely link, and relay controls to your device. Please turn on Bluetooth to connect.",
          color = PortageTextSubtitle,
          fontSize = 14.sp,
          fontFamily = GeistFontFamily,
          lineHeight = 20.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("bluetooth_dialog_description")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Status Pill: Bluetooth is disabled
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(PortageSurface3)
            .border(1.dp, PortageBorderSubtle, CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFBA71)) // warm amber dot
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Bluetooth is currently off",
              color = PortageTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = GeistFontFamily
            )
          }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Primary Action: Turn on Bluetooth
        PortagePrimaryButton(
          text = "Turn on Bluetooth",
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onEnableBluetooth()
          },
          height = 54.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "turn_on_bluetooth_button"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary / Dismiss Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              onDismiss()
            }
            .padding(vertical = 10.dp)
            .testTag("bluetooth_dialog_cancel"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Not now",
            color = PortageTextMuted,
            fontSize = 14.sp,
            fontFamily = GeistFontFamily,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
