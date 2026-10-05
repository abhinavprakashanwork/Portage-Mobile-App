package com.example.ui.screens.remap

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RemapEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PortageChip
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun RemapListScreen(
  remaps: List<RemapEntity>,
  hasUnreadNotifications: Boolean = false,
  onBackClick: () -> Unit,
  onNotificationClick: () -> Unit = {},
  onSaveRemap: (name: String, trigger: String, target: String, isActive: Boolean) -> Unit,
  onToggleActive: (id: String, isActive: Boolean) -> Unit = { _, _ -> },
  onDeleteRemap: (id: String) -> Unit
) {
  var showCreateSheet by remember { mutableStateOf(false) }
  var editingRemap by remember { mutableStateOf<RemapEntity?>(null) }

  val activeRemaps = remember(remaps) { remaps.filter { it.isActive } }
  val nonActiveRemaps = remember(remaps) { remaps.filter { !it.isActive } }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("remap_list_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
      // Top bar with back button + notification button on opposite side
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = true,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header
      ScreenHeader(
        title = "Remap Buttons",
        subtitle = "Remaps keys/buttons to other functions"
      )

      Spacer(modifier = Modifier.height(20.dp))

      if (remaps.isEmpty()) {
        EmptyStateCard(
          message = "There are no remaps created. Click button below to create one now.",
          buttonText = "Create Remap",
          onButtonClick = { showCreateSheet = true },
          modifier = Modifier.weight(1f),
          testTag = "create_remap_empty_button"
        )
        Spacer(modifier = Modifier.height(24.dp))
      } else {
        // List separated into Active Remaps and Non-Active sections
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Section 1: Active Remaps
          item {
            RemapSectionHeader(
              title = "Active Remaps",
              count = activeRemaps.size,
              testTag = "heading_active_remaps"
            )
          }

          if (activeRemaps.isEmpty()) {
            item {
              RemapEmptySectionCard(
                message = "No active remaps. Activate a remap from the 3-dot menu below.",
                testTag = "empty_active_remaps_hint"
              )
            }
          } else {
            items(activeRemaps, key = { it.id }) { remap ->
              RemapRowItem(
                remap = remap,
                onToggleActive = { active -> onToggleActive(remap.id, active) },
                onEdit = {
                  editingRemap = remap
                  showCreateSheet = true
                },
                onDelete = { onDeleteRemap(remap.id) }
              )
            }
          }

          item {
            Spacer(modifier = Modifier.height(12.dp))
          }

          // Section 2: Non-Active Remaps
          item {
            RemapSectionHeader(
              title = "Non-Active",
              count = nonActiveRemaps.size,
              testTag = "heading_non_active_remaps"
            )
          }

          if (nonActiveRemaps.isEmpty()) {
            item {
              RemapEmptySectionCard(
                message = "No non-active remaps. Use the 3-dot menu on any remap to deactivate it.",
                testTag = "empty_non_active_remaps_hint"
              )
            }
          } else {
            items(nonActiveRemaps, key = { it.id }) { remap ->
              RemapRowItem(
                remap = remap,
                onToggleActive = { active -> onToggleActive(remap.id, active) },
                onEdit = {
                  editingRemap = remap
                  showCreateSheet = true
                },
                onDelete = { onDeleteRemap(remap.id) }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pinned New Remap button (h=50)
        PortagePrimaryButton(
          text = "New Remap",
          onClick = {
            editingRemap = null
            showCreateSheet = true
          },
          height = 50.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "new_remap_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    if (showCreateSheet) {
      val defaultNewName = remember(remaps) {
        if (remaps.none { it.name.equals("Remap", ignoreCase = true) }) {
          "Remap"
        } else {
          var idx = 1
          while (remaps.any { it.name.equals("Remap ($idx)", ignoreCase = true) }) {
            idx++
          }
          "Remap ($idx)"
        }
      }

      RemapCreateSheet(
        initialName = editingRemap?.name ?: defaultNewName,
        initialTrigger = editingRemap?.triggerKey ?: "",
        initialTarget = editingRemap?.targetKey ?: "",
        onSave = { name, trigger, target ->
          // New remaps are active by default unless deactivated; editing keeps previous state
          val wasActive = editingRemap?.isActive ?: true
          if (editingRemap != null) {
            onDeleteRemap(editingRemap!!.id)
          }
          onSaveRemap(name, trigger, target, wasActive)
          showCreateSheet = false
          editingRemap = null
        },
        onDismiss = {
          showCreateSheet = false
          editingRemap = null
        }
      )
    }
  }
}

@Composable
private fun RemapSectionHeader(
  title: String,
  count: Int,
  testTag: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, bottom = 4.dp)
      .testTag(testTag)
  ) {
    Text(
      text = title,
      color = PortageText,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = GeistFontFamily
    )
    Spacer(modifier = Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(PortageSurface2)
        .border(1.dp, PortageBorder, RoundedCornerShape(10.dp))
        .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
      Text(
        text = "$count",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = GeistFontFamily
      )
    }
  }
}

@Composable
private fun RemapEmptySectionCard(
  message: String,
  testTag: String
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(PortageSurface2.copy(alpha = 0.5f))
      .border(1.dp, PortageBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .padding(vertical = 14.dp, horizontal = 16.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = message,
      color = PortageTextMuted,
      fontSize = 12.sp,
      fontFamily = GeistFontFamily
    )
  }
}

@Composable
private fun RemapRowItem(
  remap: RemapEntity,
  onToggleActive: (Boolean) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  var isMenuOpen by remember { mutableStateOf(false) }
  val haptics = LocalHapticFeedback.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(66.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(PortageSurface2)
      .border(1.dp, PortageBorder, RoundedCornerShape(20.dp))
      .clickable {
        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        onEdit()
      }
      .padding(horizontal = 16.dp)
      .testTag("remap_row_${remap.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: status dot (green if active, red if non-active) + Remap name
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .weight(1f, fill = false)
        .padding(end = 10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(if (remap.isActive) Color(0xFF22C55E) else Color(0xFFEF4444))
          .testTag("remap_dot_${remap.id}")
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = remap.name,
        color = if (remap.isActive) PortageText else PortageTextMuted,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = GeistFontFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Right: trigger -> target chips + 3-dot options menu
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PortageChip(text = remap.triggerKey)

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "maps to",
        tint = if (remap.isActive) PortageText else PortageTextMuted,
        modifier = Modifier.size(14.dp)
      )

      PortageChip(text = remap.targetKey)

      Box {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Options",
          tint = PortageText,
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable { isMenuOpen = true }
            .testTag("remap_options_${remap.id}")
        )

        DropdownMenu(
          expanded = isMenuOpen,
          onDismissRequest = { isMenuOpen = false },
          modifier = Modifier
            .background(PortageSurface2)
            .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
        ) {
          // Toggle Deactivate / Activate
          DropdownMenuItem(
            text = {
              Text(
                text = if (remap.isActive) "Deactivate" else "Activate",
                color = PortageText,
                fontFamily = GeistFontFamily
              )
            },
            onClick = {
              isMenuOpen = false
              onToggleActive(!remap.isActive)
            },
            modifier = Modifier.testTag("menu_toggle_active_${remap.id}")
          )

          DropdownMenuItem(
            text = {
              Text(
                text = "Edit",
                color = PortageText,
                fontFamily = GeistFontFamily
              )
            },
            onClick = {
              isMenuOpen = false
              onEdit()
            },
            modifier = Modifier.testTag("menu_edit_${remap.id}")
          )

          DropdownMenuItem(
            text = {
              Text(
                text = "Delete",
                color = PortageDanger,
                fontFamily = GeistFontFamily
              )
            },
            onClick = {
              isMenuOpen = false
              onDelete()
            },
            colors = MenuDefaults.itemColors(
              textColor = PortageDanger
            ),
            modifier = Modifier.testTag("menu_delete_${remap.id}")
          )
        }
      }
    }
  }
}
