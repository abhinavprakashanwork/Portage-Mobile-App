package com.example.ui.screens.workflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.VolumeKeyBus
import com.example.ui.components.BottomSheetGlow
import com.example.ui.components.HardwareKeyInterceptor
import com.example.ui.components.IntelligenceAudioFeedback
import com.example.ui.components.IntelligenceGlowOverlay
import com.example.ui.components.NumberFlow
import com.example.ui.components.PortageChip
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageSecondaryButton
import com.example.ui.components.SheetGrabHandle
import com.example.ui.components.TactileKeyBadge
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageDangerBg
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortagePrimaryHover
import com.example.ui.theme.PortagePrimarySelectedBg
import com.example.ui.theme.PortageSurface1
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepConfigSheet(
  stepNumber: Int,
  actionType: String,
  actionName: String,
  initialTitle: String? = null,
  initialPayload: String? = null,
  initialBadges: String? = null,
  onSaveStep: (title: String, actionType: String, badges: String, payload: String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Auto-generate step title or use initialTitle
  val defaultTitle = remember(actionType, actionName, initialTitle) {
    if (!initialTitle.isNullOrBlank()) {
      initialTitle
    } else {
      when (actionType) {
        "TYPE_TEXT" -> "Type Text"
        "WAIT" -> "Wait 10s"
        "KEY_SHORTCUT" -> "Key Shortcut"
        "CLICK" -> "Left Click"
        "MOVE" -> "Move Mouse (100, 100)"
        "SCROLL" -> "Scroll Down 5"
        else -> "Step $stepNumber"
      }
    }
  }

  var stepTitle by remember { mutableStateOf(defaultTitle) }

  // Action-specific state prefilled
  var textToSend by remember {
    mutableStateOf(
      if (actionType == "TYPE_TEXT" && !initialPayload.isNullOrBlank()) initialPayload else ""
    )
  }
  val capturedKeys = remember {
    val list = mutableStateListOf<String>()
    if (actionType == "KEY_SHORTCUT" && !initialBadges.isNullOrBlank()) {
      list.addAll(initialBadges.split(",").map { it.trim() }.filter { it.isNotBlank() })
    }
    list
  }
  var manualShortcut by remember { mutableStateOf("") }
  var isRecordingShortcut by remember { mutableStateOf(false) }
  val haptics = LocalHapticFeedback.current

  fun handleShortcutCaptured(key: String) {
    if (actionType == "KEY_SHORTCUT") {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      IntelligenceAudioFeedback.playChime(false)
      capturedKeys.clear()
      capturedKeys.add(key)
      stepTitle = "Press $key"
      isRecordingShortcut = false
    }
  }

  // Intercept hardware volume keys in sheet window
  HardwareKeyInterceptor()

  // Listen to physical volume keys bus
  androidx.compose.runtime.LaunchedEffect(Unit) {
    VolumeKeyBus.keyEvents.collect { key ->
      handleShortcutCaptured(key)
    }
  }

  val initialWaitSeconds = remember(initialPayload) {
    val ms = initialPayload?.toLongOrNull() ?: 10000L
    (ms / 1000L).toInt()
  }
  var minutes by remember { mutableIntStateOf(initialWaitSeconds / 60) }
  var seconds by remember { mutableIntStateOf(initialWaitSeconds % 60) }
  var isEditingMin by remember { mutableStateOf(false) }
  var isEditingSec by remember { mutableStateOf(false) }
  var minInputText by remember { mutableStateOf("") }
  var secInputText by remember { mutableStateOf("") }
  val minFocusRequester = remember { FocusRequester() }
  val secFocusRequester = remember { FocusRequester() }

  androidx.compose.runtime.LaunchedEffect(isEditingMin) {
    if (isEditingMin) {
      minFocusRequester.requestFocus()
    }
  }

  androidx.compose.runtime.LaunchedEffect(isEditingSec) {
    if (isEditingSec) {
      secFocusRequester.requestFocus()
    }
  }
  var selectedClick by remember {
    mutableStateOf(
      if (actionType == "CLICK" && !initialPayload.isNullOrBlank()) initialPayload else "Left Click"
    )
  }
  var moveX by remember { mutableIntStateOf(100) }
  var moveY by remember { mutableIntStateOf(100) }
  var scrollDirection by remember { mutableStateOf("Down") }
  var scrollAmount by remember { mutableIntStateOf(5) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PortageSurface2,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        SheetGrabHandle()
      }
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      BottomSheetGlow(modifier = Modifier.align(Alignment.BottomCenter))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(horizontal = 22.dp)
          .padding(bottom = 16.dp)
      ) {
        // Sheet Header: Step N - {Action name} + XCircle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Step $stepNumber - $actionName",
            color = PortageText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
          )

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PortageSurface3)
              .clickable { onDismiss() }
              .testTag("close_step_config_sheet"),
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

        Spacer(modifier = Modifier.height(18.dp))

        // RENAME STEP Field
        Text(
          text = "RENAME STEP",
          color = PortageTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PortageSurface3)
            .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          BasicTextField(
            value = stepTitle,
            onValueChange = { stepTitle = it },
            textStyle = TextStyle(
              color = PortageText,
              fontSize = 14.sp,
              fontFamily = PortageFontFamily
            ),
            cursorBrush = SolidColor(PortageText),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("rename_step_input")
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action-Specific Body
        when (actionType) {
          "TYPE_TEXT" -> {
            // Screenshot 1: Type Text
            Text(
              text = "ENTER YOUR TEXT TO SEND",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PortageSurface3)
                .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
              contentAlignment = Alignment.TopStart
            ) {
              BasicTextField(
                value = textToSend,
                onValueChange = {
                  textToSend = it
                  stepTitle = "Type '$it'"
                },
                textStyle = TextStyle(
                  color = PortageText,
                  fontSize = 14.sp,
                  fontFamily = PortageFontFamily
                ),
                cursorBrush = SolidColor(PortageText),
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("type_text_input")
              )
            }
          }

          "KEY_SHORTCUT" -> {
            // Screenshot 2: Key or Shortcut
            Text(
              text = "ENTER YOUR SHORTCUT KEY",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // CAPTURED row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PortageSurface1)
                .padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "CAPTURED",
                color = PortageTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
              )
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                items(capturedKeys) { k ->
                  TactileKeyBadge(text = k)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Capture box with dot grid and Inner Apple Intelligence Glow
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PortageSurface3)
                .border(
                  width = 1.dp,
                  color = PortageBorder,
                  shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                  val nextState = !isRecordingShortcut
                  isRecordingShortcut = nextState
                  IntelligenceAudioFeedback.playChime(nextState)
                }
                .testTag("shortcut_capture_box"),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val dotColor = Color(0xFF383838)
                val step = 28.dp.toPx()
                var x = step / 2
                while (x < size.width) {
                  var y = step / 2
                  while (y < size.height) {
                    drawCircle(color = dotColor, radius = 1.2.dp.toPx(), center = Offset(x, y))
                    y += step
                  }
                  x += step
                }
              }

              // Apple Intelligence Inward Perimeter Glow Overlay
              IntelligenceGlowOverlay(
                active = isRecordingShortcut,
                cornerRadius = 16.dp
              )

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
              ) {
                if (isRecordingShortcut) {
                  Text(
                    text = "Press Volume Keys to record",
                    color = PortageText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GeistFontFamily,
                    textAlign = TextAlign.Center
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = "Tap again to stop recording",
                    color = PortageTextMuted,
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily,
                    textAlign = TextAlign.Center
                  )
                } else {
                  Text(
                    text = if (capturedKeys.isEmpty()) "Tap to record key or shortcut" else "Keys: ${capturedKeys.joinToString(", ")}",
                    color = PortageText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GeistFontFamily
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Your inputs are not being captured.",
                    color = PortageTextMuted,
                    fontSize = 12.sp,
                    fontFamily = GeistFontFamily
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick shortcut chips
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(listOf("Vol +", "Vol -", "Space", "Enter", "Ctrl + C", "Ctrl + V", "Esc", "Tab")) { chip ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PortageSurface2)
                    .border(1.dp, PortageBorder, RoundedCornerShape(10.dp))
                    .clickable {
                      capturedKeys.clear()
                      capturedKeys.add(chip)
                      stepTitle = "Press $chip"
                      isRecordingShortcut = false
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = chip,
                    color = PortageText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual input row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(PortageSurface3)
                  .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                BasicTextField(
                  value = manualShortcut,
                  onValueChange = { manualShortcut = it },
                  textStyle = TextStyle(
                    color = PortageText,
                    fontSize = 13.sp,
                    fontFamily = PortageFontFamily
                  ),
                  cursorBrush = SolidColor(PortageText),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  decorationBox = { innerTextField ->
                    if (manualShortcut.isEmpty()) {
                      Text(
                        text = "Type shortcut manually like “ctrl”....",
                        color = PortageTextMuted,
                        fontSize = 12.sp
                      )
                    }
                    innerTextField()
                  }
                )
              }

              Button(
                onClick = {
                  if (manualShortcut.isNotBlank()) {
                    capturedKeys.add(manualShortcut.trim())
                    stepTitle = "Press ${manualShortcut.trim()}"
                    manualShortcut = ""
                  }
                },
                enabled = manualShortcut.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = PortagePrimary,
                  contentColor = PortageText
                ),
                modifier = Modifier
                  .width(78.dp)
                  .height(48.dp),
                contentPadding = PaddingValues(0.dp)
              ) {
                Text("Send", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          "WAIT" -> {
            // Screenshot 3: Add Delay / Wait
            Text(
              text = "ENTER THE TIME DELAY",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Time Picker Card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .height(236.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PortageSurface2)
                .border(1.dp, PortageBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                // MIN Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Increment Min",
                    tint = PortageTextMuted,
                    modifier = Modifier
                      .size(28.dp)
                      .clickable {
                        isEditingMin = false
                        isEditingSec = false
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        minutes = (minutes + 1) % 60
                        val totalSec = minutes * 60 + seconds
                        stepTitle = "Wait ${totalSec}s"
                      }
                      .testTag("increment_min_button")
                  )

                  if (isEditingMin) {
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier
                        .width(96.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PortageSurface3)
                        .border(1.5.dp, PortagePrimary, RoundedCornerShape(12.dp))
                    ) {
                      BasicTextField(
                        value = minInputText,
                        onValueChange = { newVal ->
                          val digits = newVal.filter { it.isDigit() }.take(2)
                          minInputText = digits
                          val parsed = digits.toIntOrNull() ?: 0
                          minutes = parsed.coerceIn(0, 59)
                          val totalSec = minutes * 60 + seconds
                          stepTitle = "Wait ${totalSec}s"
                        },
                        keyboardOptions = KeyboardOptions(
                          keyboardType = KeyboardType.Number,
                          imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                          onDone = {
                            isEditingMin = false
                          }
                        ),
                        textStyle = TextStyle(
                          color = PortageText,
                          fontSize = 54.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = PortageFontFamily,
                          textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(PortagePrimary),
                        singleLine = true,
                        modifier = Modifier
                          .fillMaxWidth()
                          .focusRequester(minFocusRequester)
                          .testTag("input_min_textfield"),
                        decorationBox = { innerTextField ->
                          Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                          ) {
                            if (minInputText.isEmpty()) {
                              Text(
                                text = "00",
                                style = TextStyle(
                                  color = PortageTextMuted.copy(alpha = 0.4f),
                                  fontSize = 54.sp,
                                  fontWeight = FontWeight.Bold,
                                  fontFamily = PortageFontFamily,
                                  textAlign = TextAlign.Center
                                )
                              )
                            }
                            innerTextField()
                          }
                        }
                      )
                    }
                  } else {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                          minInputText = if (minutes > 0) minutes.toString() else ""
                          isEditingMin = true
                          isEditingSec = false
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("clickable_min_display")
                    ) {
                      NumberFlow(
                        value = minutes,
                        format = "%02d",
                        digitWidth = 36.dp,
                        textStyle = TextStyle(
                          color = PortageText,
                          fontSize = 56.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = PortageFontFamily
                        ),
                        modifier = Modifier.testTag("number_flow_minutes")
                      )
                    }
                  }

                  Text(
                    text = "MIN",
                    color = PortageTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )

                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrement Min",
                    tint = PortageTextMuted,
                    modifier = Modifier
                      .size(28.dp)
                      .clickable {
                        isEditingMin = false
                        isEditingSec = false
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (minutes > 0) minutes--
                        val totalSec = minutes * 60 + seconds
                        stepTitle = "Wait ${totalSec}s"
                      }
                      .testTag("decrement_min_button")
                  )
                }

                Text(
                  text = " : ",
                  color = PortageTextMuted,
                  fontSize = 44.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .clickable {
                      isEditingMin = false
                      isEditingSec = false
                    }
                )

                // SEC Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Increment Sec",
                    tint = PortageTextMuted,
                    modifier = Modifier
                      .size(28.dp)
                      .clickable {
                        isEditingMin = false
                        isEditingSec = false
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        seconds = (seconds + 1) % 60
                        val totalSec = minutes * 60 + seconds
                        stepTitle = "Wait ${totalSec}s"
                      }
                      .testTag("increment_sec_button")
                  )

                  if (isEditingSec) {
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier
                        .width(96.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PortageSurface3)
                        .border(1.5.dp, PortagePrimary, RoundedCornerShape(12.dp))
                    ) {
                      BasicTextField(
                        value = secInputText,
                        onValueChange = { newVal ->
                          val digits = newVal.filter { it.isDigit() }.take(2)
                          secInputText = digits
                          val parsed = digits.toIntOrNull() ?: 0
                          seconds = parsed.coerceIn(0, 59)
                          val totalSec = minutes * 60 + seconds
                          stepTitle = "Wait ${totalSec}s"
                        },
                        keyboardOptions = KeyboardOptions(
                          keyboardType = KeyboardType.Number,
                          imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                          onDone = {
                            isEditingSec = false
                          }
                        ),
                        textStyle = TextStyle(
                          color = PortageText,
                          fontSize = 54.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = PortageFontFamily,
                          textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(PortagePrimary),
                        singleLine = true,
                        modifier = Modifier
                          .fillMaxWidth()
                          .focusRequester(secFocusRequester)
                          .testTag("input_sec_textfield"),
                        decorationBox = { innerTextField ->
                          Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                          ) {
                            if (secInputText.isEmpty()) {
                              Text(
                                text = "00",
                                style = TextStyle(
                                  color = PortageTextMuted.copy(alpha = 0.4f),
                                  fontSize = 54.sp,
                                  fontWeight = FontWeight.Bold,
                                  fontFamily = PortageFontFamily,
                                  textAlign = TextAlign.Center
                                )
                              )
                            }
                            innerTextField()
                          }
                        }
                      )
                    }
                  } else {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                          secInputText = if (seconds > 0) seconds.toString() else ""
                          isEditingSec = true
                          isEditingMin = false
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("clickable_sec_display")
                    ) {
                      NumberFlow(
                        value = seconds,
                        format = "%02d",
                        digitWidth = 36.dp,
                        textStyle = TextStyle(
                          color = PortageText,
                          fontSize = 56.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = PortageFontFamily
                        ),
                        modifier = Modifier.testTag("number_flow_seconds")
                      )
                    }
                  }

                  Text(
                    text = "SEC",
                    color = PortageTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )

                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrement Sec",
                    tint = PortageTextMuted,
                    modifier = Modifier
                      .size(28.dp)
                      .clickable {
                        isEditingMin = false
                        isEditingSec = false
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (seconds > 0) seconds--
                        val totalSec = minutes * 60 + seconds
                        stepTitle = "Wait ${totalSec}s"
                      }
                      .testTag("decrement_sec_button")
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Tap any number to type directly",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontFamily = GeistFontFamily,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick delay presets triggering smooth NumberFlow transitions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val quickDelays = listOf(
                "5s" to 5,
                "10s" to 10,
                "30s" to 30,
                "1m" to 60,
                "2m" to 120,
                "5m" to 300
              )
              quickDelays.forEach { (label, totalSec) ->
                val isSelected = (minutes * 60 + seconds) == totalSec
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PortagePrimary else PortageSurface2)
                    .border(
                      1.dp,
                      if (isSelected) PortagePrimary else PortageBorder,
                      RoundedCornerShape(12.dp)
                    )
                    .clickable {
                      isEditingMin = false
                      isEditingSec = false
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      minutes = totalSec / 60
                      seconds = totalSec % 60
                      stepTitle = "Wait ${totalSec}s"
                    }
                    .padding(vertical = 8.dp)
                    .testTag("preset_delay_$label"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    color = PortageText,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = GeistFontFamily
                  )
                }
              }
            }
          }

          "CLICK" -> {
            // Screenshot 4: Click
            Text(
              text = "SELECT CLICK",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val clickOptions = listOf("Left Click", "Right Click", "Double Click")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              clickOptions.forEach { opt ->
                val isSelected = selectedClick == opt
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) PortagePrimarySelectedBg else PortageSurface2)
                    .border(
                      1.dp,
                      if (isSelected) PortagePrimaryHover else PortageBorder,
                      RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      selectedClick = opt
                      stepTitle = opt
                    }
                    .padding(horizontal = 16.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = opt,
                    color = if (isSelected) PortageText else PortageTextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )

                  RadioButton(
                    selected = isSelected,
                    onClick = {
                      selectedClick = opt
                      stepTitle = opt
                    },
                    colors = RadioButtonDefaults.colors(
                      selectedColor = PortagePrimaryHover,
                      unselectedColor = PortageTextMuted
                    )
                  )
                }
              }
            }
          }

          "MOVE" -> {
            Text(
              text = "ENTER MOUSE COORDINATES (X, Y)",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(PortageSurface3)
                  .padding(14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(text = "X: $moveX px", color = PortageText, fontSize = 14.sp)
              }
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(PortageSurface3)
                  .padding(14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(text = "Y: $moveY px", color = PortageText, fontSize = 14.sp)
              }
            }
          }

          "SCROLL" -> {
            Text(
              text = "SELECT SCROLL DIRECTION",
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              listOf("Down", "Up").forEach { dir ->
                val isSelected = scrollDirection == dir
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) PortagePrimarySelectedBg else PortageSurface2)
                    .border(
                      1.dp,
                      if (isSelected) PortagePrimaryHover else PortageBorder,
                      RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      scrollDirection = dir
                      stepTitle = "Scroll $dir $scrollAmount"
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = dir,
                    color = if (isSelected) PortageText else PortageTextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        val isEditing = !initialTitle.isNullOrBlank() || !initialPayload.isNullOrBlank()

        // Footer: Primary Add action / Save Step (h=50) + Cancel (h=50)
        PortagePrimaryButton(
          text = if (isEditing) "Save Step" else "Add Action",
          onClick = {
            val (badgeStr, payloadStr) = when (actionType) {
              "TYPE_TEXT" -> "Type Text" to textToSend
              "WAIT" -> {
                val totalMs = (minutes * 60 + seconds) * 1000
                "${totalMs}ms" to "$totalMs"
              }
              "KEY_SHORTCUT" -> {
                val badges = capturedKeys.joinToString(",")
                badges to (if (capturedKeys.isNotEmpty()) capturedKeys.last() else "Win + R")
              }
              "CLICK" -> selectedClick to selectedClick
              "MOVE" -> "Move" to "$moveX,$moveY"
              "SCROLL" -> "$scrollDirection $scrollAmount" to "$scrollDirection,$scrollAmount"
              else -> "Action" to ""
            }
            onSaveStep(stepTitle, actionType, badgeStr, payloadStr)
          },
          height = 50.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "step_add_action_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PortageSecondaryButton(
          text = "Cancel",
          onClick = onDismiss,
          height = 50.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "step_cancel_button"
        )
      }
    }
  }
}
