package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.DeviceConnectionState
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun DebugTestScreen(
  connectionState: DeviceConnectionState,
  onBackClick: () -> Unit,
  onSendTest: (String) -> Unit
) {
  var text by remember { mutableStateOf("hello") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp)
  ) {
    PortageTopBar(showBackButton = true, onBackClick = onBackClick)
    Spacer(modifier = Modifier.height(24.dp))
    Text("Encrypted channel test", color = PortageText, fontSize = 24.sp, fontFamily = GeistFontFamily)
    Spacer(modifier = Modifier.height(8.dp))
    Text("State: $connectionState", color = PortageTextMuted, fontSize = 14.sp, fontFamily = GeistFontFamily)
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
      value = text,
      onValueChange = { text = it },
      label = { Text("Test string") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    PortagePrimaryButton(
      text = "Send test",
      onClick = { onSendTest(text) },
      enabled = connectionState == DeviceConnectionState.READY,
      height = 54.dp,
      modifier = Modifier.fillMaxWidth()
    )
  }
}