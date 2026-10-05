package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceProfile
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageSecondaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageDangerBg
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortageSuccess
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun DeviceSettingsScreen(
  deviceProfile: DeviceProfile,
  onBackClick: () -> Unit,
  onUpdateDevice: (name: String, type: String) -> Unit,
  onToggleConnection: () -> Unit,
  onForgetDevice: () -> Unit,
  onDebugTest: (() -> Unit)? = null
) {
  var deviceName by remember(deviceProfile.name) { mutableStateOf(deviceProfile.name) }
  var deviceType by remember(deviceProfile.type) { mutableStateOf(deviceProfile.type) }
  var isTypeMenuOpen by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val types = listOf("Laptop", "Desktop", "Tablet", "Other")

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("device_settings_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 27.dp)
    ) {
      // Top bar with back button
      PortageTopBar(
        showBackButton = true,
        onBackClick = onBackClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header
      ScreenHeader(
        title = "Device Settings",
        subtitle = "Manage your connected Portage hardware profile"
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Connection Status Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (deviceProfile.isConnected) PortageSuccess else PortageDanger)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (deviceProfile.isConnected) "Status: Connected" else "Status: Disconnected",
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Button(
          onClick = onToggleConnection,
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = PortageSurface2,
            contentColor = PortageText
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = PortageText,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (deviceProfile.isConnected) "Disconnect" else "Reconnect",
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Device Name Input
      Text(
        text = "DEVICE NAME",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        BasicTextField(
          value = deviceName,
          onValueChange = {
            deviceName = it
            onUpdateDevice(it, deviceType)
          },
          textStyle = TextStyle(
            color = PortageText,
            fontSize = 15.sp,
            fontFamily = PortageFontFamily
          ),
          cursorBrush = SolidColor(PortageText),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Device Type Select
      Text(
        text = "DEVICE TYPE",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Box(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PortageSurface3)
            .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
            .clickable { isTypeMenuOpen = true }
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = deviceType,
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = PortageText,
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = isTypeMenuOpen,
          onDismissRequest = { isTypeMenuOpen = false },
          modifier = Modifier
            .background(PortageSurface2)
            .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
        ) {
          types.forEach { t ->
            DropdownMenuItem(
              text = { Text(text = t, color = PortageText) },
              onClick = {
                deviceType = t
                isTypeMenuOpen = false
                onUpdateDevice(deviceName, t)
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Save changes button
      PortagePrimaryButton(
        text = "Save Device Profile",
        onClick = {
          onUpdateDevice(deviceName, deviceType)
          Toast.makeText(context, "Saved changes", Toast.LENGTH_SHORT).show()
        },
        height = 54.dp,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Forget Device Button (Destructive)
      Button(
        onClick = onForgetDevice,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = PortageDangerBg,
          contentColor = PortageDanger
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .border(1.dp, PortageDanger.copy(alpha = 0.5f), CircleShape)
          .testTag("forget_device_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = null,
          tint = PortageDanger,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Forget device",
          color = PortageDanger,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      if (onDebugTest != null) {
        Spacer(modifier = Modifier.height(16.dp))
        PortageSecondaryButton(
          text = "Test encrypted channel",
          onClick = onDebugTest,
          modifier = Modifier.fillMaxWidth(),
          height = 54.dp
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
