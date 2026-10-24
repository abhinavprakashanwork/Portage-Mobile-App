package com.example.ui.screens.remap

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.service.VolumeKeyBus
import com.example.ui.components.BottomSheetGlow
import com.example.ui.components.HardwareKeyInterceptor
import com.example.ui.components.IntelligenceAudioFeedback
import com.example.ui.components.IntelligenceGlowOverlay
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageSecondaryButton
import com.example.ui.components.SheetGrabHandle
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageDangerBg
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortagePrimaryHover
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemapCreateSheet(
  initialName: String = "",
  initialTrigger: String = "",
  initialTarget: String = "",
  onSave: (name: String, trigger: String, target: String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var remapName by remember { mutableStateOf(initialName.ifBlank { "Remap" }) }
  var triggerKey by remember { mutableStateOf(initialTrigger) }
  var targetKey by remember { mutableStateOf(initialTarget) }
  var activeRecordingSection by remember { mutableStateOf<String?>(null) } // "trigger" or "action"
  var showRenameDialog by remember { mutableStateOf(false) }
  var renameDialogInput by remember { mutableStateOf(remapName) }
  val haptics = LocalHapticFeedback.current

  fun handleKeyCaptured(key: String) {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    IntelligenceAudioFeedback.playChime(false)
    if (activeRecordingSection == "action") {
      targetKey = key
      activeRecordingSection = null
    } else if (activeRecordingSection == "trigger") {
      triggerKey = key
      activeRecordingSection = null
    } else {
      if (triggerKey.isBlank()) {
        triggerKey = key
      } else if (targetKey.isBlank()) {
        targetKey = key
      } else {
        triggerKey = key
      }
    }
  }

  // Intercept hardware volume keys inside this ModalBottomSheet window
  HardwareKeyInterceptor()

  // Global listener for volume hardware keys in sheet
  LaunchedEffect(Unit) {
    VolumeKeyBus.keyEvents.collect { key ->
      handleKeyCaptured(key)
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PortageSurface2,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        SheetGrabHandle()
      }
    }
  ) {
    // Hugs content tightly without giant empty space at bottom
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
          .padding(horizontal = 24.dp)
          .padding(top = 6.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Remap Title with Pencil Icon: Tapping opens Rename dialog window
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              renameDialogInput = remapName
              showRenameDialog = true
            }
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("remap_title_clickable_row")
        ) {
          Text(
            text = remapName.ifBlank { "Remap" },
            color = PortageText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = InstrumentSerifFontFamily,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f, fill = false)
              .widthIn(max = 160.dp)
          )
          Spacer(modifier = Modifier.width(14.dp))
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(PortageSurface3),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Rename remap",
              tint = PortageTextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spacious Card 1: WHEN YOU PRESS
        RemapSectionCard(
          sectionTitle = "WHEN YOU PRESS",
          badgeLabel = "Trigger",
          activeKey = triggerKey,
          isRecordingMode = activeRecordingSection == "trigger",
          onStartRecording = {
            activeRecordingSection = "trigger"
            IntelligenceAudioFeedback.playChime(true)
          },
          onStopRecording = {
            activeRecordingSection = null
            IntelligenceAudioFeedback.playChime(false)
          },
          onKeySelected = {
            triggerKey = it
            activeRecordingSection = null
          },
          quickSuggestions = listOf("Vol +", "Vol -", "Power", "F12")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Spacious Circular Arrow Divider
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF333333)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = PortageText,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spacious Card 2: YOU TRIGGER
        RemapSectionCard(
          sectionTitle = "YOU TRIGGER",
          badgeLabel = "Action",
          activeKey = targetKey,
          isRecordingMode = activeRecordingSection == "action",
          onStartRecording = {
            activeRecordingSection = "action"
            IntelligenceAudioFeedback.playChime(true)
          },
          onStopRecording = {
            activeRecordingSection = null
            IntelligenceAudioFeedback.playChime(false)
          },
          onKeySelected = {
            targetKey = it
            activeRecordingSection = null
          },
          quickSuggestions = listOf("Space", "Enter", "Ctrl+C", "Esc")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: Save Remap & Cancel
        PortagePrimaryButton(
          text = "Save Remap",
          onClick = {
            val finalName = remapName.ifBlank {
              if (triggerKey.isNotBlank() && targetKey.isNotBlank()) "$triggerKey to $targetKey" else "Remap"
            }
            onSave(
              finalName,
              triggerKey.ifBlank { "Vol +" },
              targetKey.ifBlank { "Space" }
            )
          },
          height = 52.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "save_remap_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PortageSecondaryButton(
          text = "Cancel",
          onClick = onDismiss,
          height = 52.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "cancel_remap_button"
        )
      }
    }
  }

  // Rename Dialog Window (Same as Renaming Workflow)
  if (showRenameDialog) {
    AlertDialog(
      onDismissRequest = { showRenameDialog = false },
      containerColor = PortageSurface2,
      title = {
        Text(
          text = "Rename Remap",
          color = PortageText,
          fontFamily = InstrumentSerifFontFamily,
          fontSize = 22.sp
        )
      },
      text = {
        Column {
          Text(
            text = "Enter a new name for this remap:",
            color = PortageTextMuted,
            fontSize = 13.sp,
            fontFamily = GeistFontFamily
          )
          Spacer(modifier = Modifier.height(12.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(PortageSurface3)
              .border(1.dp, PortagePrimaryHover, RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            BasicTextField(
              value = renameDialogInput,
              onValueChange = { renameDialogInput = it },
              textStyle = TextStyle(
                color = PortageText,
                fontSize = 15.sp,
                fontFamily = GeistFontFamily
              ),
              cursorBrush = SolidColor(PortagePrimaryHover),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("rename_remap_dialog_input")
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (renameDialogInput.isNotBlank()) {
              remapName = renameDialogInput.trim()
            }
            showRenameDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PortagePrimary)
        ) {
          Text("Save", color = PortageText)
        }
      },
      dismissButton = {
        TextButton(onClick = { showRenameDialog = false }) {
          Text("Cancel", color = PortageTextMuted)
        }
      }
    )
  }
}

/**
 * Section card with full-width "Type Manually" button and volume button listener
 */
@Composable
private fun RemapSectionCard(
  sectionTitle: String,
  badgeLabel: String,
  activeKey: String,
  isRecordingMode: Boolean,
  onStartRecording: () -> Unit,
  onStopRecording: () -> Unit,
  onKeySelected: (String) -> Unit,
  quickSuggestions: List<String>
) {
  var showManualInput by remember { mutableStateOf(false) }
  var manualText by remember { mutableStateOf("") }
  val haptics = LocalHapticFeedback.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .background(PortageSurface2)
      .border(1.dp, PortageBorder, RoundedCornerShape(22.dp))
      .padding(16.dp)
  ) {
    // Header Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = sectionTitle,
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
      Text(
        text = badgeLabel,
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontFamily = GeistFontFamily
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Main Interactive Area
    if (activeKey.isNotBlank() && !isRecordingMode) {
      // Clean captured key state matching user design
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onStartRecording()
          }
          .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = if (badgeLabel == "Trigger") "Trigger button assigned" else "Action button assigned",
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Blue rounded button/chip with key
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(PortagePrimary)
              .padding(horizontal = 16.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = activeKey,
              color = PortageText,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = GeistFontFamily
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (badgeLabel == "Trigger") "Tap to change trigger" else "Tap to change action",
            color = PortageTextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    } else if (isRecordingMode) {
      // Active Recording trigger box with Inner Apple Intelligence Glow matching user request
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(88.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
          .clickable {
            onStopRecording()
          },
        contentAlignment = Alignment.Center
      ) {
        // Inner perimeter glow overlay
        IntelligenceGlowOverlay(
          active = true,
          cornerRadius = 16.dp
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
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
        }
      }
    } else {
      // Empty prompt box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(84.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(PortageSurface3.copy(alpha = 0.6f))
          .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onStartRecording()
          }
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Tap to Record Key",
            color = PortageText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "or press volume hardware key",
            color = PortageTextMuted,
            fontSize = 12.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (!showManualInput) {
      // "Type Manually" button fills the ENTIRE width of the card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(PortageSurface3)
          .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            showManualInput = true
          },
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Keyboard,
            contentDescription = null,
            tint = PortageTextMuted,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Type Manually",
            color = PortageText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    } else {
      // When clicked: Suggestions pop up above it AND manual entry text field appears
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Suggestions Header & Hide
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SUGGESTIONS",
            color = PortageTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Hide",
            color = PortageTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
              .clickable { showManualInput = false }
              .padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Suggestions pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          quickSuggestions.forEach { suggestion ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (activeKey == suggestion) PortagePrimary else PortageSurface3)
                .border(1.dp, PortageBorder, RoundedCornerShape(10.dp))
                .clickable {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onKeySelected(suggestion)
                  onStopRecording()
                  showManualInput = false
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = suggestion,
                color = PortageText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual Input Row + Set button
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(PortageSurface3)
              .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            BasicTextField(
              value = manualText,
              onValueChange = { manualText = it },
              textStyle = TextStyle(
                color = PortageText,
                fontSize = 13.sp,
                fontFamily = GeistFontFamily
              ),
              cursorBrush = SolidColor(PortagePrimaryHover),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              decorationBox = { innerTextField ->
                if (manualText.isEmpty()) {
                  Text(
                    text = "Type shortcut (e.g. Ctrl+Z, Space)…",
                    color = PortageTextMuted,
                    fontSize = 12.sp
                  )
                }
                innerTextField()
              }
            )
          }

          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (manualText.isNotBlank()) PortagePrimary else PortageSurface3)
              .clickable {
                if (manualText.isNotBlank()) {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onKeySelected(manualText.trim())
                  manualText = ""
                  showManualInput = false
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Set key",
              tint = if (manualText.isNotBlank()) PortageText else PortageTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
