package com.example.ui.screens.sendtext

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransmissionEntity
import com.example.device.DeviceConnectionState
import com.example.service.PrivacyModeManager
import com.example.ui.components.BottomSheetGlow
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageSecondaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SheetGrabHandle
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SendTextScreen(
  transmissions: List<TransmissionEntity>,
  connectionState: DeviceConnectionState,
  onBackClick: () -> Unit,
  onNotificationClick: () -> Unit = {},
  onSendText: suspend (String) -> Boolean,
  onClearTransmissions: () -> Unit
) {
  var textInput by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  var showHistorySheet by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val isPrivacyMode by PrivacyModeManager.isPrivacyMode.collectAsState()
  val isConnected = connectionState == DeviceConnectionState.READY

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("send_text_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 27.dp)
    ) {
      // Top bar with back button and notification button
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = true,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header
      ScreenHeader(
        title = "Send Text",
        subtitle = "Type and send texts or commands to the device."
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Big Textarea
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(24.dp))
          .background(PortageSurface3)
          .padding(18.dp)
          .testTag("send_textarea"),
        contentAlignment = Alignment.TopStart
      ) {
        BasicTextField(
          value = textInput,
          onValueChange = { textInput = it },
          visualTransformation = if (isPrivacyMode) PasswordVisualTransformation() else VisualTransformation.None,
          textStyle = TextStyle(
            color = PortageText,
            fontSize = 15.sp,
            fontFamily = GeistFontFamily,
            lineHeight = 22.sp
          ),
          cursorBrush = SolidColor(PortageText),
          modifier = Modifier.fillMaxSize(),
          decorationBox = { innerTextField ->
            if (textInput.isEmpty()) {
              Text(
                text = "Try “hello” or “Ctrl + A”",
                color = PortageTextMuted,
                fontSize = 15.sp
              )
            }
            innerTextField()
          }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Hint row with "Press Ctrl + Enter to quickly send" and Privacy Mode toggle button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Press Ctrl + Enter to quickly send",
          color = PortageTextMuted,
          fontSize = 12.sp,
          fontFamily = GeistFontFamily
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              PrivacyModeManager.toggle(context)
            }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("privacy_mode_toggle")
        ) {
          Icon(
            imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = "Privacy Mode",
            tint = if (isPrivacyMode) PortagePrimary else PortageTextMuted,
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Privacy Mode",
            color = if (isPrivacyMode) PortagePrimary else PortageTextMuted,
            fontSize = 12.sp,
            fontWeight = if (isPrivacyMode) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = GeistFontFamily
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (!isConnected) {
        Text(
          text = when (connectionState) {
            DeviceConnectionState.SCANNING, DeviceConnectionState.CONNECTING, DeviceConnectionState.PAIRING -> "Connecting to device…"
            DeviceConnectionState.NEEDS_PAIRING -> "Pair your device before sending text."
            DeviceConnectionState.UNSUPPORTED -> "This device firmware is not supported."
            is DeviceConnectionState.ERROR -> connectionState.reason
            DeviceConnectionState.DISCONNECTED -> "Device not connected"
            DeviceConnectionState.READY -> ""
          },
          color = PortageTextMuted,
          fontSize = 12.sp,
          fontFamily = GeistFontFamily
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Send Button (Height 54)
      PortagePrimaryButton(
        text = if (isSending) "Sending…" else "Send",
        icon = Icons.AutoMirrored.Filled.Send,
        onClick = {
          if (!isConnected) {
            Toast.makeText(context, "Device not connected", Toast.LENGTH_SHORT).show()
            return@PortagePrimaryButton
          }
          if (textInput.isNotBlank()) {
            isSending = true
            scope.launch {
              val success = runCatching { onSendText(textInput) }.getOrDefault(false)
              isSending = false
              if (success) {
                Toast.makeText(context, "Sent to device", Toast.LENGTH_SHORT).show()
                textInput = ""
              } else {
                Toast.makeText(context, "Could not send text. Check the device connection and try again.", Toast.LENGTH_LONG).show()
              }
            }
          }
        },
        enabled = textInput.isNotBlank() && !isSending,
        height = 54.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "send_button"
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Previous Transmission Button with dynamic count (Height 54)
      PortageSecondaryButton(
        text = "Previous Transmissions (${transmissions.size})",
        onClick = { showHistorySheet = true },
        height = 54.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "prev_transmissions_button"
      )

      Spacer(modifier = Modifier.height(24.dp))
    }

    if (showHistorySheet) {
      PreviousTransmissionsSheet(
        transmissions = transmissions,
        isPrivacyMode = isPrivacyMode,
        onSelect = { selected ->
          textInput = selected
          showHistorySheet = false
        },
        onClearAll = onClearTransmissions,
        onDismiss = { showHistorySheet = false }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousTransmissionsSheet(
  transmissions: List<TransmissionEntity>,
  isPrivacyMode: Boolean = false,
  onSelect: (String) -> Unit,
  onClearAll: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PortageSurface2,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        SheetGrabHandle()
      }
    }
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      BottomSheetGlow(modifier = Modifier.align(Alignment.BottomCenter))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp)
          .padding(bottom = 16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Previous Transmissions",
            color = PortageText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
          )

          if (transmissions.isNotEmpty()) {
            Text(
              text = "Clear all",
              color = com.example.ui.theme.PortageDanger,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier
                .clickable { onClearAll() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("clear_all_transmissions")
            )
            Spacer(modifier = Modifier.width(6.dp))
          }

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PortageSurface3)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = PortageText,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        HorizontalDivider(color = PortageBorderSubtle, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        if (transmissions.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No previous transmissions recorded",
              color = PortageTextMuted,
              fontSize = 14.sp
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(transmissions) { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .background(PortageSurface3)
                  .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
                  .clickable { onSelect(item.text) }
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isPrivacyMode) "••••••••" else item.text,
                    color = PortageText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Schedule,
                      contentDescription = null,
                      tint = PortageTextMuted,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = dateFormat.format(Date(item.timestamp)),
                      color = PortageTextMuted,
                      fontSize = 11.sp
                    )
                  }
                }
                Text(
                  text = "Use",
                  color = PortageTextMuted,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  }
}
