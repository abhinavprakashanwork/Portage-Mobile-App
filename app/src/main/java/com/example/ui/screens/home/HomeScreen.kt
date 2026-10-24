package com.example.ui.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DeviceProfile
import com.example.device.DeviceConnectionState
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.OnestFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageSuccess
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun HomeScreen(
  deviceProfile: DeviceProfile,
  connectionState: DeviceConnectionState,
  hasUnreadNotifications: Boolean = false,
  onNavigateToSendText: () -> Unit,
  onNavigateToLiveRelay: () -> Unit,
  onNavigateToRemap: () -> Unit,
  onNavigateToWorkflows: () -> Unit,
  onNavigateToVirtualControl: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNotificationClick: () -> Unit = {}
) {
  val haptics = LocalHapticFeedback.current

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("home_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top bar title with notification button on right
      PortageTopBar(
        showBackButton = false,
        showNotificationButton = true,
        hasUnreadNotifications = hasUnreadNotifications,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Status Pill: centered bg --surface-3, radius 999, 8px dot + "Connected"
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(PortageSurface3)
          .padding(horizontal = 14.dp, vertical = 6.dp)
          .testTag("connection_status_pill"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (connectionState == DeviceConnectionState.READY) PortageSuccess else PortageDanger)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when (connectionState) {
              DeviceConnectionState.READY -> "Connected"
              DeviceConnectionState.SCANNING, DeviceConnectionState.CONNECTING, DeviceConnectionState.PAIRING -> "Connecting"
              DeviceConnectionState.NEEDS_PAIRING -> "Pairing required"
              DeviceConnectionState.UNSUPPORTED -> "Unsupported firmware"
              is DeviceConnectionState.ERROR -> "Connection failed"
              DeviceConnectionState.DISCONNECTED -> "Not connected"
            },
            color = PortageText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Laptop Illustration (reduced size, balanced proportions)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.laptop_device),
          contentDescription = "Device illustration",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Device Name in main Heading Font (Instrument Serif)
      Text(
        text = deviceProfile.name,
        color = PortageText,
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = HeadingFontFamily,
        textAlign = TextAlign.Center,
        letterSpacing = (-0.3).sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("device_name_home")
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Battery status: Battery icon + "57% Charge"
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.testTag("battery_status")
      ) {
        Icon(
          imageVector = Icons.Default.BatteryChargingFull,
          contentDescription = "Battery",
          tint = PortageText,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${deviceProfile.batteryPercent}% Charge",
          color = PortageTextMuted,
          fontSize = 14.sp,
          fontFamily = GeistFontFamily,
          fontWeight = FontWeight.Normal
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2x3 Symmetrical Tile Grid (left margin 22, tiles 175x133 approx, gap 8)
      // Row 1: Send Text & Live Relay
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HomeActionTile(
          title = "Send Text",
          subtitle = "Send long texts easily",
          icon = Icons.Default.ContentPaste,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToSendText()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_send_text"
        )
        HomeActionTile(
          title = "Live Relay",
          subtitle = "Send keys in real time",
          icon = Icons.Default.Radio,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToLiveRelay()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_live_relay"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Row 2: Remap Buttons & Workflows
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HomeActionTile(
          title = "Remap Buttons",
          subtitle = "Remap buttons to another function",
          icon = Icons.Default.SwapHoriz,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToRemap()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_remap_buttons"
        )
        HomeActionTile(
          title = "Workflows",
          subtitle = "Save repeatable actions",
          icon = Icons.Default.Route,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToWorkflows()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_workflows"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Row 3: Virtual Control & Device Settings (Symmetrical 2x3 Grid)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HomeActionTile(
          title = "Virtual Control",
          subtitle = "Touchpad, remotes.",
          icon = Icons.Default.Link,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToVirtualControl()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_virtual_control"
        )
        HomeActionTile(
          title = "Device Settings",
          subtitle = "Configure settings",
          icon = Icons.Default.Settings,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToSettings()
          },
          modifier = Modifier.weight(1f),
          testTag = "tile_device_settings"
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Footer: Portage App 3.0.0
      Text(
        text = "Portage App 3.0.0",
        color = PortageTextMuted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun HomeActionTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Column(
    modifier = modifier
      .height(133.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(PortageSurface2)
      .border(1.dp, PortageBorder, RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .padding(16.dp)
      .testTag(testTag),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = title,
        color = PortageText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        color = PortageTextMuted,
        fontSize = 12.sp,
        lineHeight = 16.sp
      )
    }

    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PortageText,
      modifier = Modifier.size(18.dp)
    )
  }
}
