package com.example.ui.screens.onboarding

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.BluetoothPromptDialog
import com.example.ui.components.HelpLink
import com.example.ui.components.HelpSheet
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.device.DeviceConnectionState
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import com.example.ui.theme.PortageTextSubtitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ConnectionState {
  IDLE,
  CONNECTING,
  CONNECTED,
  ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDeviceScreen(
  connectionState: DeviceConnectionState,
  onConnect: () -> Unit,
  onPair: (String) -> Unit,
  onConnected: () -> Unit
) {
  val context = LocalContext.current
  val bluetoothManager = remember {
    context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  }
  var isBluetoothEnabled by remember {
    mutableStateOf(bluetoothManager?.adapter?.isEnabled == true)
  }
  var showBluetoothDialog by remember { mutableStateOf(false) }
  var showHelpSheet by remember { mutableStateOf(false) }
  var pairingKey by remember { mutableStateOf("") }
  val visualState = when (connectionState) {
    DeviceConnectionState.SCANNING, DeviceConnectionState.CONNECTING, DeviceConnectionState.PAIRING -> ConnectionState.CONNECTING
    DeviceConnectionState.READY -> ConnectionState.CONNECTED
    is DeviceConnectionState.ERROR, DeviceConnectionState.UNSUPPORTED -> ConnectionState.ERROR
    else -> ConnectionState.IDLE
  }

  LaunchedEffect(connectionState) {
    if (connectionState == DeviceConnectionState.READY) onConnected()
  }

  // System enable Bluetooth launcher
  val enableBluetoothLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val enabled = bluetoothManager?.adapter?.isEnabled == true || result.resultCode == Activity.RESULT_OK
    isBluetoothEnabled = enabled
    showBluetoothDialog = false
    if (enabled) onConnect()
  }

  val requestBluetoothPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    if (permissions.values.all { it }) {
      enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }
  }

  fun triggerEnableBluetooth() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val required = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
      if (required.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
        requestBluetoothPermissionLauncher.launch(required)
        return
      }
    } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestBluetoothPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
      return
    }

    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    try {
      enableBluetoothLauncher.launch(enableBtIntent)
    } catch (e: Exception) {
      // Handles emulators or environments without standard Bluetooth activity
      isBluetoothEnabled = true
      showBluetoothDialog = false
      onConnect()
    }
  }

  fun handleConnectClick() {
    // If Bluetooth is disabled, prompt the user with the dedicated Bluetooth dialog
    if (!isBluetoothEnabled) {
      showBluetoothDialog = true
    } else {
      onConnect()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("connect_device_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
      // Top bar without back button
      PortageTopBar(showBackButton = false)

      Spacer(modifier = Modifier.height(20.dp))

      // Header H1 in Instrument Serif + Subtitle
      Text(
        text = "Connect your device",
        color = PortageText,
        fontSize = 38.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = InstrumentSerifFontFamily,
        letterSpacing = (-0.5).sp,
        lineHeight = 42.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Plug your Portage device into any USB port then click Connect below.",
        color = PortageTextSubtitle,
        fontSize = 14.sp,
        fontFamily = GeistFontFamily,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Device Panel
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(320.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
          .testTag("device_panel"),
        contentAlignment = Alignment.Center
      ) {
        when (visualState) {
          ConnectionState.IDLE -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(24.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(if (isBluetoothEnabled) PortagePrimary.copy(alpha = 0.15f) else Color(0x26FFAA00))
                  .border(
                    1.dp,
                    if (isBluetoothEnabled) PortagePrimary.copy(alpha = 0.40f) else Color(0x55FFAA00),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Bluetooth,
                  contentDescription = "Bluetooth Status",
                  tint = if (isBluetoothEnabled) PortagePrimary else Color(0xFFFFBA71),
                  modifier = Modifier.size(32.dp)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (isBluetoothEnabled) "Bluetooth Ready" else "Bluetooth Required",
                color = PortageText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GeistFontFamily
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = if (isBluetoothEnabled)
                  "Ready to discover and securely link with your Portage device."
                else
                  "Portage requires Bluetooth to discover and pair your device.",
                color = PortageTextMuted,
                fontSize = 13.sp,
                fontFamily = GeistFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
              )

              if (!isBluetoothEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(PortageSurface2)
                    .border(1.dp, PortageBorderSubtle, CircleShape)
                    .clickable { showBluetoothDialog = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("bluetooth_status_pill")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFBA71))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Turn on Bluetooth",
                      color = PortagePrimary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      fontFamily = GeistFontFamily
                    )
                  }
                }
              }
            }
          }
          ConnectionState.CONNECTING -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              CircularProgressIndicator(
                color = PortagePrimary,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Looking for device…",
                color = PortageTextMuted,
                fontSize = 14.sp
              )
            }
          }
          ConnectionState.ERROR -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Cable,
                contentDescription = null,
                tint = PortageTextMuted,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No device found",
                color = PortageTextMuted,
                fontSize = 14.sp
              )
            }
          }
          ConnectionState.CONNECTED -> {
            // Handled via navigation
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Connect Button
      PortagePrimaryButton(
        text = if (visualState == ConnectionState.CONNECTING) "Connecting…" else "Connect",
        onClick = { handleConnectClick() },
        enabled = visualState != ConnectionState.CONNECTING,
        height = 60.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "connect_button"
      )

      Spacer(modifier = Modifier.height(8.dp))

      HelpLink(onClick = { showHelpSheet = true })

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Bluetooth Required Dialog Prompt
    if (showBluetoothDialog) {
      BluetoothPromptDialog(
        onDismiss = { showBluetoothDialog = false },
        onEnableBluetooth = { triggerEnableBluetooth() }
      )
    }

    if (showHelpSheet) {
      HelpSheet(onDismiss = { showHelpSheet = false })
    }

    if (connectionState == DeviceConnectionState.NEEDS_PAIRING) {
      ModalBottomSheet(onDismissRequest = {}) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Pair your ToothPaste", color = PortageText, fontSize = 22.sp, fontFamily = InstrumentSerifFontFamily)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Hold the button on your ToothPaste until it types a key, copy it, paste it here.",
            color = PortageTextMuted,
            fontSize = 14.sp,
            fontFamily = GeistFontFamily
          )
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
            value = pairingKey,
            onValueChange = { pairingKey = it.filterNot(Char::isWhitespace) },
            label = { Text("Public key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          TextButton(
            onClick = { onPair(pairingKey) },
            enabled = pairingKey.length >= 44,
            modifier = Modifier.align(Alignment.End)
          ) { Text("Pair") }
        }
      }
    }
  }
}
