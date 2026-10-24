package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import com.example.ui.theme.PortageTextSubtitle

@Composable
fun DeviceInfoScreen(
  onFinish: (name: String, type: String) -> Unit
) {
  var selectedType by remember { mutableStateOf("Laptop") }
  var deviceName by remember { mutableStateOf("Lenovo Yogabook") }
  var isDropdownExpanded by remember { mutableStateOf(false) }
  var fieldWidth by remember { androidx.compose.runtime.mutableIntStateOf(0) }
  var showHelpSheet by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val density = LocalDensity.current
  val haptics = LocalHapticFeedback.current

  val deviceTypes = listOf("Laptop", "Desktop", "Tablet", "Other")

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("device_info_screen")
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
        text = "Device Information",
        color = PortageText,
        fontSize = 38.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = InstrumentSerifFontFamily,
        letterSpacing = (-0.5).sp,
        lineHeight = 42.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Select the type of device and name it",
        color = PortageTextSubtitle,
        fontSize = 14.sp,
        fontFamily = GeistFontFamily
      )

      Spacer(modifier = Modifier.height(36.dp))

      // Device Type Dropdown
      Text(
        text = "Select device type from dropdown",
        color = PortageTextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Box(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .onGloballyPositioned { coordinates ->
              fieldWidth = coordinates.size.width
            }
            .clip(RoundedCornerShape(16.dp))
            .background(PortageSurface3)
            .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
            .clickable {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              isDropdownExpanded = true
            }
            .padding(horizontal = 16.dp)
            .testTag("device_type_dropdown"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = selectedType,
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select type",
            tint = PortageText,
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = isDropdownExpanded,
          onDismissRequest = { isDropdownExpanded = false },
          modifier = Modifier
            .width(if (fieldWidth > 0) with(density) { fieldWidth.toDp() } else 300.dp)
            .background(PortageSurface2)
            .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
        ) {
          deviceTypes.forEach { type ->
            DropdownMenuItem(
              text = {
                Text(
                  text = type,
                  color = PortageText,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium
                )
              },
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedType = type
                isDropdownExpanded = false
              },
              colors = MenuDefaults.itemColors(
                textColor = PortageText
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Device Name Input
      Text(
        text = "Name this device",
        color = PortageTextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        BasicTextField(
          value = deviceName,
          onValueChange = {
            if (it.length <= 32) deviceName = it
          },
          textStyle = TextStyle(
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = GeistFontFamily
          ),
          cursorBrush = SolidColor(PortageText),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("device_name_input"),
          decorationBox = { innerTextField ->
            if (deviceName.isEmpty()) {
              Text(
                text = "e.g. My Laptop",
                color = PortageTextMuted,
                fontSize = 15.sp
              )
            }
            innerTextField()
          }
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Finish Button
      PortagePrimaryButton(
        text = "Finish",
        onClick = { onFinish(deviceName.trim(), selectedType) },
        enabled = deviceName.isNotBlank(),
        height = 60.dp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "finish_button"
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
